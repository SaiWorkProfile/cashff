package com.cashff.service;

import com.cashff.repository.RedeemCodeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Component
public class RedeemCodeExpiryCleanup {
    private final RedeemCodeRepository codes;
    private final Clock clock;

    public RedeemCodeExpiryCleanup(RedeemCodeRepository codes, Clock clock) {
        this.codes = codes;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.redeem-codes.cleanup-interval-ms:1000}")
    @Transactional
    public void deleteExpiredCodes() {
        codes.deleteByEndAtLessThanEqual(clock.instant());
    }
}
