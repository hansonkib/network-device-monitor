package com.hanson.network_device_monitor.model;

public enum NetworkDeviceType {

    CPE("Customer Premises Equipment"),
    ROUTER("Router"),
    SWITCH("Switch"),
    ACCESS_POINT("Access Point"),
    FIREWALL("Firewall"),
    ONT("optical network terminals"),
    OTHER("Other Network Devices");

    private String name;

    NetworkDeviceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
