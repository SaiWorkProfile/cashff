package com.cashff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "custom_rooms")
public class CustomRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String roomCode;

    @Column(nullable = false, length = 100)
    private String passwordCode = "111";

    @Column(nullable = false, length = 80)
    private String slotTime;

    @Column(nullable = false)
    private Instant publishedAt = Instant.now();

    @Column(nullable = false)
    private Instant unlockAt;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(length = 500)
    private String firstPhoto;

    @Column(length = 500)
    private String secondPhoto;

    @Column(length = 500)
    private String thirdPhoto;

    public CustomRoom() {
    }

    public CustomRoom(String roomCode, String passwordCode, String slotTime) {
        this.roomCode = roomCode;
        this.passwordCode = passwordCode;
        this.slotTime = slotTime;
        this.publishedAt = Instant.now();
        this.unlockAt = this.publishedAt.plusSeconds(180);
    }

    public CustomRoom(String roomCode, String passwordCode, Instant startAt, Instant endAt, Instant publishedAt) {
        this.roomCode = roomCode;
        this.passwordCode = passwordCode;
        this.slotTime = "";
        this.startAt = startAt;
        this.endAt = endAt;
        this.publishedAt = publishedAt;
        this.unlockAt = startAt.plusSeconds(180);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getPasswordCode() {
        return passwordCode;
    }

    public void setPasswordCode(String passwordCode) {
        this.passwordCode = passwordCode;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getUnlockAt() {
        return unlockAt;
    }

    public void setUnlockAt(Instant unlockAt) {
        this.unlockAt = unlockAt;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public String getFirstPhoto() {
        return firstPhoto;
    }

    public void setFirstPhoto(String firstPhoto) {
        this.firstPhoto = firstPhoto;
    }

    public String getSecondPhoto() {
        return secondPhoto;
    }

    public void setSecondPhoto(String secondPhoto) {
        this.secondPhoto = secondPhoto;
    }

    public String getThirdPhoto() {
        return thirdPhoto;
    }

    public void setThirdPhoto(String thirdPhoto) {
        this.thirdPhoto = thirdPhoto;
    }
}
