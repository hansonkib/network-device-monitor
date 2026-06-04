package com.hanson.network_device_monitor.model;

public enum NetworkDeviceStatus {
    ONLINE("Online"),
    OFFLINE("Offline"),
    DEGRADED("Degraded");

    private String name;

    NetworkDeviceStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
