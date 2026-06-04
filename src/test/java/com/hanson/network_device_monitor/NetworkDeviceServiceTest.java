package com.hanson.network_device_monitor;

import com.hanson.network_device_monitor.dto.NetworkDeviceDto.DeviceDetailResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.DeviceSummaryResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.RegisterDeviceRequest;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.StatusReportResponse;
import com.hanson.network_device_monitor.dto.NetworkDeviceDto.SubmitStatusRequest;
import com.hanson.network_device_monitor.exception.NetworkDeviceNotFoundException;
import com.hanson.network_device_monitor.model.NetworkDevice;
import com.hanson.network_device_monitor.model.NetworkDeviceStatus;
import com.hanson.network_device_monitor.model.NetworkDeviceStatusReport;
import com.hanson.network_device_monitor.model.NetworkDeviceType;
import com.hanson.network_device_monitor.repository.NetworkDeviceRepository;
import com.hanson.network_device_monitor.repository.NetworkDeviceStatusReportRepository;
import com.hanson.network_device_monitor.service.NetworkDeviceService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NetworkDeviceServiceTest {

    @Mock
    private NetworkDeviceRepository deviceRepository;

    @Mock
    private NetworkDeviceStatusReportRepository statusReportRepository;

    @InjectMocks
    private NetworkDeviceService deviceService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(deviceService, "staleThresholdMinutes", 15);
    }


    @Test
    void registerDevice_shouldPersistAndReturnSummary() {
        RegisterDeviceRequest req = new RegisterDeviceRequest("Router-01", NetworkDeviceType.ROUTER, "192.168.1.1", "HQ");
        NetworkDevice saved = NetworkDevice.builder()
                .id(UUID.randomUUID())
                .name("Router-01")
                .deviceType(NetworkDeviceType.ROUTER)
                .host("192.168.1.1")
                .location("HQ")
                .registeredAt(OffsetDateTime.now())
                .build();

        when(deviceRepository.save(any(NetworkDevice.class))).thenReturn(saved);

        DeviceSummaryResponse result = deviceService.registerDevice(req);

        assertThat(result.name()).isEqualTo("Router-01");
        assertThat(result.deviceType()).isEqualTo(NetworkDeviceType.ROUTER);
        assertThat(result.stale()).isTrue(); // no reports yet → stale
        verify(deviceRepository).save(any(NetworkDevice.class));
    }

    // ── Submit Status ─────────────────────────────────────────────────────────

    @Test
    void submitStatus_shouldCreateReport() {
        UUID id = UUID.randomUUID();
        NetworkDevice device = NetworkDevice.builder().id(id).name("Switch-01").registeredAt(OffsetDateTime.now()).build();
        NetworkDeviceStatusReport report = NetworkDeviceStatusReport.builder()
                .id(UUID.randomUUID())
                .device(device)
                .status(NetworkDeviceStatus.ONLINE)
                .reportedAt(OffsetDateTime.now())
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
        when(statusReportRepository.save(any(NetworkDeviceStatusReport.class))).thenReturn(report);

        StatusReportResponse result = deviceService.submitStatus(id, new SubmitStatusRequest(NetworkDeviceStatus.ONLINE, null));

        assertThat(result.status()).isEqualTo(NetworkDeviceStatus.ONLINE);
        verify(statusReportRepository).save(any(NetworkDeviceStatusReport.class));
    }

    @Test
    void submitStatus_shouldThrowWhenDeviceNotFound() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.submitStatus(id, new SubmitStatusRequest(NetworkDeviceStatus.ONLINE, null)))
                .isInstanceOf(NetworkDeviceNotFoundException.class);
    }


    @Test
    void listAll_deviceWithRecentReport_shouldNotBeStale() {
        NetworkDevice device = NetworkDevice.builder()
                .id(UUID.randomUUID()).name("AP-01").registeredAt(OffsetDateTime.now()).build();
        NetworkDeviceStatusReport recent = NetworkDeviceStatusReport.builder()
                .status(NetworkDeviceStatus.ONLINE)
                .reportedAt(OffsetDateTime.now().minusMinutes(5)) // 5 min ago → fresh
                .build();

        when(deviceRepository.findAll()).thenReturn(List.of(device));
        when(statusReportRepository.findTopByDeviceOrderByReportedAtDesc(device)).thenReturn(Optional.of(recent));

        List<DeviceSummaryResponse> results = deviceService.listAll();
        assertThat(results.get(0).stale()).isFalse();
    }

    @Test
    void listAll_deviceWithOldReport_shouldBeStale() {
        NetworkDevice device = NetworkDevice.builder()
                .id(UUID.randomUUID()).name("AP-02").registeredAt(OffsetDateTime.now()).build();
        NetworkDeviceStatusReport old = NetworkDeviceStatusReport.builder()
                .status(NetworkDeviceStatus.OFFLINE)
                .reportedAt(OffsetDateTime.now().minusMinutes(20)) // 20 min ago → stale
                .build();

        when(deviceRepository.findAll()).thenReturn(List.of(device));
        when(statusReportRepository.findTopByDeviceOrderByReportedAtDesc(device)).thenReturn(Optional.of(old));

        List<DeviceSummaryResponse> results = deviceService.listAll();
        assertThat(results.get(0).stale()).isTrue();
    }

    @Test
    void listAll_deviceWithNoReports_shouldBeStale() {
        NetworkDevice device = NetworkDevice.builder()
                .id(UUID.randomUUID()).name("FW-01").registeredAt(OffsetDateTime.now()).build();

        when(deviceRepository.findAll()).thenReturn(List.of(device));
        when(statusReportRepository.findTopByDeviceOrderByReportedAtDesc(device)).thenReturn(Optional.empty());

        List<DeviceSummaryResponse> results = deviceService.listAll();
        assertThat(results.get(0).stale()).isTrue();
    }


    @Test
    void getDevice_shouldReturnLast20Reports() {
        UUID id = UUID.randomUUID();
        NetworkDevice device = NetworkDevice.builder().id(id).name("ONT-01").registeredAt(OffsetDateTime.now()).build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
        when(statusReportRepository.findByDeviceOrderByReportedAtDesc(eq(device), any(PageRequest.class)))
                .thenReturn(List.of());

        DeviceDetailResponse result = deviceService.getDevice(id);
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.recentReports().isEmpty());
    }

    @Test
    void getDevice_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDevice(id))
                .isInstanceOf(NetworkDeviceNotFoundException.class);
    }

}
