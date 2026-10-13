package com.cashff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "donation_screenshots")
public class DonationScreenshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    public DonationScreenshot() {
    }

    public DonationScreenshot(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Donation getDonation() {
        return donation;
    }

    public void setDonation(Donation donation) {
        if (this.donation == donation) {
            if (donation != null && !donation.getScreenshots().contains(this)) {
                donation.getScreenshots().add(this);
            }
            return;
        }

        Donation previousDonation = this.donation;
        if (previousDonation != null) {
            previousDonation.getScreenshots().remove(this);
        }

        this.donation = donation;
        if (donation != null && !donation.getScreenshots().contains(this)) {
            donation.getScreenshots().add(this);
        }
    }
}
