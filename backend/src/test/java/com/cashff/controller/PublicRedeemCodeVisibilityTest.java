package com.cashff.controller;

import com.cashff.entity.RedeemCode;
import com.cashff.repository.AppSettingRepository;
import com.cashff.repository.CustomRoomRepository;
import com.cashff.repository.DonationRepository;
import com.cashff.repository.DonorSlotRepository;
import com.cashff.repository.RedeemCodeRepository;
import com.cashff.service.CustomRoomSchedule;
import com.cashff.service.FileStorageService;
import com.cashff.service.ScheduledBannerService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicRedeemCodeVisibilityTest {
    @Test
    void onlyReturnsCodesDuringTheirScheduledWindow() {
        Instant start = Instant.parse("2026-10-13T10:00:00Z");
        Instant end = Instant.parse("2026-10-13T11:00:00Z");
        Clock clock = Clock.fixed(start, ZoneId.of("Asia/Kolkata"));
        RedeemCode activeCode = new RedeemCode("ACTIVE-CODE", start, end);
        activeCode.setId(1L);

        RedeemCodeRepository codes = mock(RedeemCodeRepository.class);
        when(codes.findByStartAtLessThanEqualAndEndAtGreaterThanOrderByCreatedAtDesc(start, start))
                .thenReturn(List.of(activeCode));
        PublicController controller = new PublicController(
                mock(DonorSlotRepository.class), codes, mock(CustomRoomRepository.class),
                mock(AppSettingRepository.class), mock(DonationRepository.class),
                mock(FileStorageService.class), new CustomRoomSchedule(clock),
                mock(ScheduledBannerService.class), clock);

        List<Map<String, Object>> visibleCodes = controller.redeemCodes();

        assertEquals(1, visibleCodes.size());
        assertEquals("ACTIVE-CODE", visibleCodes.get(0).get("code"));
        assertEquals("Active", visibleCodes.get(0).get("status"));
        assertEquals(true, visibleCodes.get(0).get("canCopy"));
    }
}
