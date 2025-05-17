package com.example.demo_springboot.restservice;

import com.example.demo_springboot.resourceObjects.Battery;
import java.util.List;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

public class BatteryStatsResponse extends RepresentationModel<BatteryStatsResponse> {
    private List<EntityModel<Battery>> batteries;
    private long totalWattCapacity;
    private double averageWattCapacity;

    public BatteryStatsResponse(List<EntityModel<Battery>> batteries, long totalWattCapacity, double averageWattCapacity) {
        this.batteries = batteries;
        this.totalWattCapacity = totalWattCapacity;
        this.averageWattCapacity = averageWattCapacity;
    }

    public List<EntityModel<Battery>> getBatteries() {
        return batteries;
    }

    public long getTotalWattCapacity() {
        return totalWattCapacity;
    }

    public double getAverageWattCapacity() {
        return averageWattCapacity;
    }
}
