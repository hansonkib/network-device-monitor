package com.hanson.network_device_monitor.exception;

import java.util.UUID;

public class NetworkDeviceNotFoundException extends RuntimeException {

    public NetworkDeviceNotFoundException(UUID id) {

        super("Device not found with");

    }

}
