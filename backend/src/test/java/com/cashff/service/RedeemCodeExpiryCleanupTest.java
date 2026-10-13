package com.cashff.service;

import com.cashff.repository.RedeemCodeRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RedeemCodeExpiryCleanupTest {
    @Test
    void deletesCodesWhoseEndTimeHasArrived() {
        Instant now = Instant.parse("2026-10-13T10:00:00Z");
        RedeemCodeRepository codes = mock(RedeemCodeRepository.class);
        RedeemCodeExpiryCleanup cleanup = new RedeemCodeExpiryCleanup(
                codes, Clock.fixed(now, ZoneId.of("Asia/Kolkata")));

        cleanup.deleteExpiredCodes();

        verify(codes).deleteByEndAtLessThanEqual(now);
    }
}
