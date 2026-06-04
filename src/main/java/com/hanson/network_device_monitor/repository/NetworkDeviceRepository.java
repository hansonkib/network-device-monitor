package com.hanson.network_device_monitor.repository;


import com.hanson.network_device_monitor.model.NetworkDevice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, UUID> {

    @Query("""
        SELECT DISTINCT d FROM NetworkDevice d
        LEFT JOIN FETCH d.statusReports sr
        WHERE sr.id IS NULL
           OR sr.reportedAt = (
               SELECT MAX(sr2.reportedAt)
               FROM NetworkDeviceStatusReport sr2
               WHERE sr2.device = d
           )
        ORDER BY d.registeredAt DESC
        """)
    List<NetworkDevice> findAllWithLatestReport();

}
