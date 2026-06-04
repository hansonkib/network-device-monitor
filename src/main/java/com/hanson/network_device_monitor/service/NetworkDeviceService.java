package com.hanson.network_device_monitor.service;

import com.hanson.network_device_monitor.dto.NetworkDeviceDto.DeviceDetailResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.DeviceSummaryResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.RegisterDeviceRequest;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.StatusReportResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.SubmitStatusRequest;
import com.hanson.network_device_monitor.exception.NetworkDeviceNotFoundException;
import com.hanson.network_device_monitor.model.NetworkDevice;
import com.hanson.network_device_monitor.model.NetworkDeviceStatusReport;
import com.hanson.network_device_monitor.repository.NetworkDeviceRepository;
import com.hanson.network_device_monitor.repository.NetworkDeviceStatusReportRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetworkDeviceService {

    private final NetworkDeviceRepository deviceRepository;
    private final NetworkDeviceStatusReportRepository statusReportRepository;

    @Value("${app.stale-threshold-minutes:15}")
    private int staleThresholdMinutes;

    @Transactional
    public DeviceSummaryResponse registerDevice(RegisterDeviceRequest request) {
        NetworkDevice device = NetworkDevice.builder()
                .name(request.name())
                .deviceType(request.deviceType())
                .host(request.host())
                .location(request.location())
                .registeredAt(OffsetDateTime.now())
                .build();
        device = deviceRepository.save(device);
        return toSummary(device, null);
    }


    @Transactional
    public StatusReportResponse submitStatus(UUID deviceId, SubmitStatusRequest request) {
        NetworkDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NetworkDeviceNotFoundException(deviceId));

        NetworkDeviceStatusReport report = NetworkDeviceStatusReport.builder()
                .device(device)
                .status(request.status())
                .message(request.message())
                .reportedAt(OffsetDateTime.now())
                .build();
        report = statusReportRepository.save(report);
        return toReportResponse(report);
    }


    public List<DeviceSummaryResponse> listAll() {
        List<NetworkDevice> devices = deviceRepository.findAll();
        return devices.stream()
                .map(d -> {
                    Optional<NetworkDeviceStatusReport> latest = statusReportRepository.findTopByDeviceOrderByReportedAtDesc(d);
                    return toSummary(d, latest.orElse(null));
                })
                .toList();
    }


    public DeviceDetailResponse getDevice(UUID deviceId) {
        NetworkDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NetworkDeviceNotFoundException(deviceId));

        List<NetworkDeviceStatusReport> reports = statusReportRepository.findByDeviceOrderByReportedAtDesc(
                device, PageRequest.of(0, 20));

        NetworkDeviceStatusReport latest = reports.isEmpty() ? null : reports.get(0);

        List<StatusReportResponse> reportResponses = reports.stream()
                .map(this::toReportResponse)
                .toList();

        return new DeviceDetailResponse(
                device.getId(),
                device.getName(),
                device.getDeviceType(),
                device.getHost(),
                device.getLocation(),
                device.getRegisteredAt(),
                latest != null ? latest.getStatus() : null,
                latest != null ? latest.getReportedAt() : null,
                isStale(latest != null ? latest.getReportedAt() : null),
                reportResponses
        );
    }


    private DeviceSummaryResponse toSummary(NetworkDevice device, NetworkDeviceStatusReport latest) {
        return new DeviceSummaryResponse(
                device.getId(),
                device.getName(),
                device.getDeviceType(),
                device.getHost(),
                device.getLocation(),
                device.getRegisteredAt(),
                latest != null ? latest.getStatus() : null,
                latest != null ? latest.getReportedAt() : null,
                isStale(latest != null ? latest.getReportedAt() : null)
        );
    }

    private StatusReportResponse toReportResponse(NetworkDeviceStatusReport report) {
        return new StatusReportResponse(
                report.getId(),
                report.getStatus(),
                report.getMessage(),
                report.getReportedAt()
        );
    }

    private boolean isStale(OffsetDateTime lastReportedAt) {
        if (lastReportedAt == null) return true;
        return lastReportedAt.isBefore(OffsetDateTime.now().minusMinutes(staleThresholdMinutes));
    }

}
