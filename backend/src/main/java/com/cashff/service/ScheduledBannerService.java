package com.cashff.service;

import com.cashff.entity.AppSetting;
import com.cashff.entity.ScheduledBanner;
import com.cashff.repository.AppSettingRepository;
import com.cashff.repository.ScheduledBannerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduledBannerService {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final ScheduledBannerRepository banners;
    private final AppSettingRepository settings;
    private final FileStorageService storage;
    private final Clock clock;

    public ScheduledBannerService(ScheduledBannerRepository banners, AppSettingRepository settings,
                                  FileStorageService storage, Clock clock) {
        this.banners = banners;
        this.settings = settings;
        this.storage = storage;
        this.clock = clock;
    }

    @Transactional
    public ScheduledBanner schedule(MultipartFile image, String startValue, String endValue) {
        Instant now = clock.instant();
        Instant startAt = parseIstDateTime(startValue, "Start date and time");
        Instant endAt = parseIstDateTime(endValue, "End date and time");
        if (startAt.isBefore(now)) {
            throw new IllegalArgumentException("Banner start time cannot be in the past (IST).");
        }
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("Banner end time must be later than its start time (IST).");
        }

        String imageUrl = storage.storeImage(image);
        try {
            AppSetting legacyBanner = settings.findById("bannerUrl").orElse(null);
            if (legacyBanner != null) {
                legacyBanner.setSettingValue("");
                settings.save(legacyBanner);
            }
            return banners.save(new ScheduledBanner(imageUrl, startAt, endAt, now));
        } catch (RuntimeException exception) {
            storage.deleteStoredImage(imageUrl);
            throw exception;
        }
    }

    public List<ScheduledBanner> findAll() {
        return banners.findAllByOrderByStartAtAsc();
    }

    public Optional<ScheduledBanner> findActive() {
        Instant now = clock.instant();
        return banners.findFirstByStartAtLessThanEqualAndEndAtGreaterThanOrderByStartAtDesc(now, now);
    }

    @Transactional
    public void delete(long id) {
        ScheduledBanner banner = banners.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled banner not found."));
        storage.deleteStoredImage(banner.getImageUrl());
        banners.delete(banner);
    }

    @Transactional
    public void deleteExpired() {
        List<ScheduledBanner> expired = banners.findByEndAtLessThanEqual(clock.instant());
        for (ScheduledBanner banner : expired) {
            storage.deleteStoredImage(banner.getImageUrl());
        }
        banners.deleteAll(expired);
    }

    private Instant parseIstDateTime(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return LocalDateTime.parse(value.trim()).atZone(IST).toInstant();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid " + label.toLowerCase() + " value.", exception);
        }
    }
}
