package com.hanson.network_device_monitor.dto;

import com.hanson.network_device_monitor.model.NetworkDeviceStatus;
import com.hanson.network_device_monitor.model.NetworkDeviceType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NetworkDeviceDto {


    public record RegisterDeviceRequest(
            @NotBlank(message = "NetworkDevice name is required") String name,
            @NotNull(message = "NetworkDevice type is required") NetworkDeviceType deviceType,
            @NotBlank(message = "Host or IP is required") String host,
            @NotBlank(message = "Location is required") String location
    ) {}

    public record SubmitStatusRequest(
            @NotNull(message = "NetworkDeviceStatus is required") NetworkDeviceStatus status,
            String message
    ) {}


    public record StatusReportResponse(
            UUID id,
            NetworkDeviceStatus status,
            String message,
            OffsetDateTime reportedAt
    ) {}

    public record DeviceSummaryResponse(
            UUID id,
            String name,
            NetworkDeviceType deviceType,
            String host,
            String location,
            OffsetDateTime registeredAt,
            NetworkDeviceStatus currentStatus,
            OffsetDateTime lastReportedAt,
            boolean stale
    ) {}

    public record DeviceDetailResponse(
            UUID id,
            String name,
            NetworkDeviceType deviceType,
            String host,
            String location,
            OffsetDateTime registeredAt,
            NetworkDeviceStatus currentStatus,
            OffsetDateTime lastReportedAt,
            boolean stale,
            List<StatusReportResponse> recentReports
    ) {}

}
