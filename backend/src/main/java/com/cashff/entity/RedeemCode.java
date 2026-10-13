package com.cashff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "redeem_codes")
public class RedeemCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String code;

    @Column(name = "scheduled_time", nullable = false, length = 80)
    private String scheduledTime;

    @Column(nullable = true)
    private Instant startAt;

    @Column(nullable = true)
    private Instant endAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public RedeemCode() {
    }

    public RedeemCode(String code, Instant startAt, Instant endAt) {
        this.code = code;
        this.startAt = startAt;
        this.endAt = endAt;
        this.scheduledTime = formatScheduledTime(startAt, endAt);
        this.createdAt = Instant.now();
    }

    public RedeemCode(String code, String scheduledTime) {
        this.code = code;
        this.startAt = null;
        this.endAt = null;
        this.scheduledTime = scheduledTime == null ? "" : scheduledTime;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
        this.scheduledTime = formatScheduledTime(this.startAt, this.endAt);
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
        this.scheduledTime = formatScheduledTime(this.startAt, this.endAt);
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime == null ? "" : scheduledTime;
        this.startAt = null;
        this.endAt = null;
    }

    private static String formatScheduledTime(Instant startAt, Instant endAt) {
        if (startAt == null && endAt == null) {
            return "";
        }
        if (startAt == null) {
            return endAt.toString();
        }
        if (endAt == null) {
            return startAt.toString();
        }
        return startAt + " -> " + endAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatusAt(Instant reference) {
        if (startAt == null || endAt == null) {
            return "Legacy";
        }
        if (reference.isBefore(startAt)) {
            return "Scheduled";
        }
        if (!reference.isBefore(endAt)) {
            return "Expired";
        }
        return "Active";
    }

    public boolean canCopyAt(Instant reference) {
        return "Active".equals(getStatusAt(reference));
    }
}
