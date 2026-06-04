package com.hanson.network_device_monitor.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "status_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkDeviceStatusReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private NetworkDevice device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NetworkDeviceStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "reported_at", nullable = false)
    private OffsetDateTime reportedAt;

    @PrePersist
    public void prePersist() {
        if (reportedAt == null) {
            reportedAt = OffsetDateTime.now();
        }
    }

}
