package com.example.demo_springboot.restservice;

import java.util.stream.Collectors;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo_springboot.resourceObjects.Battery;
import com.example.demo_springboot.resourceObjects.BatteryListRequest;
import com.example.demo_springboot.resourceObjects.BatteryRepository;
import com.example.demo_springboot.resourceObjects.FilterRangeObject;
import com.example.demo_springboot.restservice.exceptionsAndAdvices.BatteryNotFoundException;
import com.example.demo_springboot.restservice.exceptionsAndAdvices.InvalidPostcodeRangeException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping(path="/batteryservice")
public class BatteryServiceController {
  private static final Logger logger = LoggerFactory.getLogger(BatteryServiceController.class);

	private BatteryRepository repository;
  private final BatteryModelAssembler assembler;

	BatteryServiceController(BatteryRepository repository, BatteryModelAssembler assembler) {
    	this.repository = repository;
      this.assembler = assembler;
  	}

  @GetMapping("/test")
  public String test() {
      logger.info(">>> Hello from /test endpoint <<<");
      return "test";
  }

	// Aggregate root
  // tag::get-aggregate-root[]
  @GetMapping("/batteries")
  CollectionModel<EntityModel<Battery>> getAllBatteries() {
    List<EntityModel<Battery>> batteries = repository.findAll().stream()
      .map(assembler::toModel)
      .collect(Collectors.toList());
    return CollectionModel.of(batteries, linkTo(methodOn(BatteryServiceController.class).getAllBatteries()).withSelfRel());
  }
  // end::get-aggregate-root[]

  @PostMapping("/batteries")
  ResponseEntity<CollectionModel<EntityModel<Battery>>> newBattery(@RequestBody BatteryListRequest request) {
    List<Battery> savedBatteries = repository.saveAll(request.getBatteries());

    List<EntityModel<Battery>> entityModels = savedBatteries.stream()
        .map(assembler::toModel)
        .toList();

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(CollectionModel.of(entityModels));
  }
  
  @GetMapping("/batteries/{id}")
  EntityModel<Battery> getBattery(@PathVariable Long id) {
    Battery battery = repository.findById(id) //
      .orElseThrow(() -> new BatteryNotFoundException(id));

    return assembler.toModel(battery);
  }

  @PutMapping("/batteries/{id}")
  ResponseEntity<?> replaceBattery(@RequestBody Battery newBattery, @PathVariable Long id) {
    Battery updateBattery = repository.findById(id)
      .map(battery -> {
        battery.setName(newBattery.getName());
        battery.setPostcode(newBattery.getPostcode());
        return repository.save(battery);
      })
      .orElseGet(() -> {
        return repository.save(newBattery);
      });

    EntityModel<Battery> entityModel = assembler.toModel(updateBattery);

    return ResponseEntity
      .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
      .body(entityModel);
  }

  @DeleteMapping("/batteries/{id}")
  ResponseEntity<?> deleteBattery(@PathVariable Long id) {
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/batteries/bypostcode")
  ResponseEntity<BatteryStatsResponse> filterByPostcodeRange(@RequestBody FilterRangeObject range) {
    if (range.getFrom() == null && range.getTo() == null) {
      throw new InvalidPostcodeRangeException("Both 'from' and 'to' postcodes cannot be null.");
    }

    // Convert and validate postcodes
    String fromPostcode = range.getFrom() != null ? validateAndConvertPostcode(range.getFrom()) : null;
    String toPostcode = range.getTo() != null ? validateAndConvertPostcode(range.getTo()) : null;

    // If either 'from' or 'to' is missing, adjust the query
    List<Battery> batteries;
    if (fromPostcode != null && toPostcode == null) {
      // Query based only on the 'from' postcode
      batteries = repository.findByPostcodeGreaterThanEqualOrderByNameAsc(fromPostcode);
    } else if (toPostcode != null && fromPostcode == null) {
      // Query based only on the 'to' postcode
      batteries = repository.findByPostcodeLessThanEqualOrderByNameAsc(toPostcode);
    } else {
      // Both 'from' and 'to' are provided, so use the range
      batteries = repository.findByPostcodeBetweenOrderByNameAsc(fromPostcode, toPostcode);
    }

    List<EntityModel<Battery>> batteryModels = batteries.stream().map(assembler::toModel).collect(Collectors.toList());

    // Compute stats
    long totalCapacity = batteries.stream().mapToLong(Battery::getCapacity).sum();
    double averageCapacity = batteries.isEmpty() ? 0.0 : totalCapacity / (double) batteries.size();

    BatteryStatsResponse response = new BatteryStatsResponse(batteryModels, totalCapacity, averageCapacity);
    // Add hypermedia links
    response.add(linkTo(methodOn(BatteryServiceController.class).filterByPostcodeRange(range)).withSelfRel());
    response.add(linkTo(methodOn(BatteryServiceController.class).getAllBatteries()).withRel("all-batteries"));
    
    return ResponseEntity.ok(response);
  }

  private String validateAndConvertPostcode(String postcode) {
    try {
        Long postcodeLong = Long.valueOf(postcode);
        return postcode;
    } catch (NumberFormatException e) {
        throw new InvalidPostcodeRangeException("Postcode must be a valid number.");
    }
}
  
}