package com.example.demo_springboot.restservice;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.example.demo_springboot.resourceObjects.Battery;

@Component
public class BatteryModelAssembler implements RepresentationModelAssembler<Battery, EntityModel<Battery>> {

  @Override
  public EntityModel<Battery> toModel(Battery battery) {

    return EntityModel.of(battery, //
        linkTo(methodOn(BatteryServiceController.class).getBattery(battery.getId())).withSelfRel(),
        linkTo(methodOn(BatteryServiceController.class).getAllBatteries()).withRel("all-batteries"));
  }
}
