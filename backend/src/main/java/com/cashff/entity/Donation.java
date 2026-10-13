package com.cashff.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String whatsapp;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<DonationScreenshot> screenshots = new ArrayList<>();

    public Donation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<DonationScreenshot> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<DonationScreenshot> screenshots) {
        List<DonationScreenshot> replacement = screenshots == null
                ? List.of()
                : new ArrayList<>(screenshots);

        for (DonationScreenshot screenshot : new ArrayList<>(this.screenshots)) {
            if (!replacement.contains(screenshot)) {
                screenshot.setDonation(null);
            }
        }

        this.screenshots.removeIf(screenshot -> !replacement.contains(screenshot));
        for (DonationScreenshot screenshot : replacement) {
            if (screenshot == null) {
                continue;
            }
            if (screenshot.getDonation() != this) {
                screenshot.setDonation(this);
            } else if (!this.screenshots.contains(screenshot)) {
                this.screenshots.add(screenshot);
            }
        }
    }

    public void addScreenshot(DonationScreenshot screenshot) {
        Objects.requireNonNull(screenshot, "screenshot");
        if (screenshot.getDonation() == this) {
            if (!this.screenshots.contains(screenshot)) {
                this.screenshots.add(screenshot);
            }
            return;
        }
        screenshot.setDonation(this);
    }
}
