package com.cashff.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledBannerExpiryCleanup {
    private final ScheduledBannerService banners;

    public ScheduledBannerExpiryCleanup(ScheduledBannerService banners) {
        this.banners = banners;
    }

    @Scheduled(fixedDelayString = "${app.banners.cleanup-interval-ms:1000}")
    public void deleteExpiredBanners() {
        banners.deleteExpired();
    }
}
