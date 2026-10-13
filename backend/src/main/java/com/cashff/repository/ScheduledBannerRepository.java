package com.cashff.repository;

import com.cashff.entity.ScheduledBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ScheduledBannerRepository extends JpaRepository<ScheduledBanner, Long> {
    Optional<ScheduledBanner> findFirstByStartAtLessThanEqualAndEndAtGreaterThanOrderByStartAtDesc(
            Instant startAt, Instant endAt);

    List<ScheduledBanner> findAllByOrderByStartAtAsc();

    List<ScheduledBanner> findByEndAtLessThanEqual(Instant endAt);
}
