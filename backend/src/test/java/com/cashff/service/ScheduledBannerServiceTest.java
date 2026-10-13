package com.cashff.service;

import com.cashff.entity.ScheduledBanner;
import com.cashff.entity.AppSetting;
import com.cashff.repository.AppSettingRepository;
import com.cashff.repository.ScheduledBannerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledBannerServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-13T10:00:00Z");

    @Test
    void schedulesDatetimeLocalValuesAsIstAndClearsTheLegacyBanner() {
        ScheduledBannerRepository banners = mock(ScheduledBannerRepository.class);
        AppSettingRepository settings = mock(AppSettingRepository.class);
        FileStorageService storage = mock(FileStorageService.class);
        AppSetting legacy = new AppSetting("bannerUrl", "/uploads/old-banner.png");
        when(storage.storeImage(any())).thenReturn("/uploads/scheduled-banner.png");
        when(settings.findById("bannerUrl")).thenReturn(Optional.of(legacy));
        when(banners.save(any(ScheduledBanner.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ScheduledBannerService service = service(banners, settings, storage);

        ScheduledBanner saved = service.schedule(mock(MultipartFile.class),
                "2099-01-01T10:00", "2099-01-01T11:00");

        assertEquals("/uploads/scheduled-banner.png", saved.getImageUrl());
        assertEquals(Instant.parse("2099-01-01T04:30:00Z"), saved.getStartAt());
        assertEquals(Instant.parse("2099-01-01T05:30:00Z"), saved.getEndAt());
        assertEquals("", legacy.getSettingValue());
        verify(settings).save(legacy);
        verify(banners).save(saved);
    }

    @Test
    void rejectsPastStartWithoutSavingImage() {
        FileStorageService storage = mock(FileStorageService.class);
        ScheduledBannerService service = service(mock(ScheduledBannerRepository.class),
                mock(AppSettingRepository.class), storage);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.schedule(mock(MultipartFile.class),
                        "2000-01-01T10:00", "2000-01-01T11:00"));

        assertEquals("Banner start time cannot be in the past (IST).", error.getMessage());
        verify(storage, never()).storeImage(any());
    }

    @Test
    void rejectsEndTimeThatIsNotLaterThanStartWithoutSavingImage() {
        FileStorageService storage = mock(FileStorageService.class);
        ScheduledBannerService service = service(mock(ScheduledBannerRepository.class),
                mock(AppSettingRepository.class), storage);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.schedule(mock(MultipartFile.class),
                        "2099-01-01T10:00", "2099-01-01T10:00"));

        assertEquals("Banner end time must be later than its start time (IST).", error.getMessage());
        verify(storage, never()).storeImage(any());
    }

    @Test
    void deletesExpiredBannerRowsAndTheirUploadedImages() {
        ScheduledBanner expired = new ScheduledBanner("/uploads/expired-banner.png",
                NOW.minusSeconds(3600), NOW, NOW.minusSeconds(7200));
        ScheduledBannerRepository banners = mock(ScheduledBannerRepository.class);
        FileStorageService storage = mock(FileStorageService.class);
        when(banners.findByEndAtLessThanEqual(NOW)).thenReturn(List.of(expired));
        ScheduledBannerService service = service(banners, mock(AppSettingRepository.class), storage);

        service.deleteExpired();

        verify(storage).deleteStoredImage("/uploads/expired-banner.png");
        verify(banners).deleteAll(List.of(expired));
    }

    private ScheduledBannerService service(ScheduledBannerRepository banners,
                                             AppSettingRepository settings,
                                             FileStorageService storage) {
        return new ScheduledBannerService(banners, settings, storage,
                Clock.fixed(NOW, ZoneId.of("Asia/Kolkata")));
    }
}
