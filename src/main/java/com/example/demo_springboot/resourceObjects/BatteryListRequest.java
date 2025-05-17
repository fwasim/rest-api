package com.example.demo_springboot.resourceObjects;

import java.util.List;

public class BatteryListRequest {
    private List<Battery> batteries;

    public BatteryListRequest() {}

    public BatteryListRequest(List<Battery> batteries) {
        this.batteries = batteries;
    }

    public List<Battery> getBatteries() {
        return batteries;
    }

    public void setBatteries(List<Battery> batteries) {
        this.batteries = batteries;
    }
}
