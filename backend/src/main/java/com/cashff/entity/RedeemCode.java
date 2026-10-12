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
        this.createdAt = Instant.now();
    }

    public RedeemCode(String code, String scheduledTime) {
        this.code = code;
        this.startAt = null;
        this.endAt = null;
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
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public String getScheduledTime() {
        if (startAt == null && endAt == null) {
            return "";
        }
        if (startAt == null) {
            return endAt == null ? "" : endAt.toString();
        }
        if (endAt == null) {
            return startAt.toString();
        }
        return startAt + " -> " + endAt;
    }

    public void setScheduledTime(String scheduledTime) {
        this.startAt = null;
        this.endAt = null;
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
