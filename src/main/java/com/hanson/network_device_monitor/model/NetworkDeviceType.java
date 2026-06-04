package com.hanson.network_device_monitor.model;

public enum NetworkDeviceType {

    CPE("Cpe"),
    ROUTER("Router"),
    SWITCH("Switch"),
    ACCESS_POINT("Access Point"),
    FIREWALL("Firewall"),
    ONT("Ont"),
    OTHER("Other");

    private String name;

    NetworkDeviceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
