package com.cashff.repository;

import com.cashff.entity.CustomRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CustomRoomRepository extends JpaRepository<CustomRoom, Long> {
    List<CustomRoom> findByStartAtLessThanEqualAndEndAtGreaterThanOrderByPublishedAtDesc(
            Instant startAt, Instant endAt);

    void deleteByEndAtLessThanEqual(Instant endAt);

    List<CustomRoom> findAllByOrderByPublishedAtDesc();
}
