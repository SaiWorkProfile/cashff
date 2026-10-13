package com.cashff.repository;
import com.cashff.entity.RedeemCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RedeemCodeRepository extends JpaRepository<RedeemCode, Long> {
    List<RedeemCode> findByStartAtLessThanEqualAndEndAtGreaterThanOrderByCreatedAtDesc(
            Instant startAt, Instant endAt);

    void deleteByEndAtLessThanEqual(Instant endAt);

    List<RedeemCode> findAllByOrderByCreatedAtDesc();
}
