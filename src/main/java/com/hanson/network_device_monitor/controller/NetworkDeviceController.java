package com.hanson.network_device_monitor.controller;

import com.hanson.network_device_monitor.dto.NetworkDeviceDto;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.DeviceDetailResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.DeviceSummaryResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.StatusReportResponse;
import com.hanson.network_device_monitor.service.NetworkDeviceService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class NetworkDeviceController {

    private final NetworkDeviceService deviceService;

    /**
     * POST /api/devices
     * Register a new device.
     */
    @PostMapping
    public ResponseEntity<DeviceSummaryResponse> registerDevice(
            @Valid @RequestBody NetworkDeviceDto.RegisterDeviceRequest request) {
        DeviceSummaryResponse response = deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/devices
     * List all registered devices with current status and stale indicator.
     */
    @GetMapping
    public ResponseEntity<List<DeviceSummaryResponse>> listDevices() {
        return ResponseEntity.ok(deviceService.listAll());
    }

    /**
     * GET /api/devices/{id}
     * View a device with its 20 most recent status reports.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailResponse> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getDevice(id));
    }

    /**
     * POST /api/devices/{id}/status
     * Submit a status report for a device.
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<StatusReportResponse> submitStatus(
            @PathVariable UUID id,
            @Valid @RequestBody NetworkDeviceDto.SubmitStatusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.submitStatus(id, request));
    }

}
