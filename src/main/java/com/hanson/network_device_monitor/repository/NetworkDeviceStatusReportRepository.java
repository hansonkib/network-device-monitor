package com.hanson.network_device_monitor.repository;

import com.hanson.network_device_monitor.model.NetworkDevice;
import com.hanson.network_device_monitor.model.NetworkDeviceStatusReport;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NetworkDeviceStatusReportRepository extends JpaRepository<NetworkDeviceStatusReport, UUID> {

    List<NetworkDeviceStatusReport> findByDeviceOrderByReportedAtDesc(NetworkDevice device, Pageable pageable);

    @Query("SELECT sr FROM NetworkDeviceStatusReport sr WHERE sr.device = :device ORDER BY sr.reportedAt DESC")
    List<NetworkDeviceStatusReport> findLatestByDevice(NetworkDevice device, Pageable pageable);

    Optional<NetworkDeviceStatusReport> findTopByDeviceOrderByReportedAtDesc(NetworkDevice device);

}
