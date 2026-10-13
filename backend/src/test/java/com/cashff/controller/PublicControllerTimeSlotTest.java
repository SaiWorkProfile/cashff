package com.cashff.controller;

import com.cashff.entity.DonorSlot;
import com.cashff.entity.CustomRoom;
import com.cashff.repository.AppSettingRepository;
import com.cashff.repository.CustomRoomRepository;
import com.cashff.repository.DonationRepository;
import com.cashff.repository.DonorSlotRepository;
import com.cashff.repository.RedeemCodeRepository;
import com.cashff.service.FileStorageService;
import com.cashff.service.CustomRoomSchedule;
import com.cashff.service.ScheduledBannerService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicControllerTimeSlotTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Test
    void beforeNineAm_keepsAllPublicSlotDetailsHidden() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-01T08:59:59+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertNull(response.get("activeSlot"));
        assertTrue(slots.get("9:00 AM").isEmpty());
        assertTrue(slots.get("5:00 PM").isEmpty());
        assertTrue(slots.get("9:00 PM").isEmpty());
    }

    @Test
    void atNineAm_makesMorningSlotPublic() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-01T09:00:00+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertEquals("9:00 AM", response.get("activeSlot"));
        assertEquals(List.of("Morning Star"), slots.get("9:00 AM").stream().map(slot -> slot.get("name")).toList());
        assertTrue(slots.get("5:00 PM").isEmpty());
        assertTrue(slots.get("9:00 PM").isEmpty());
    }

    @Test
    void atFivePm_switchesToEveningSlot() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-01T17:00:00+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertEquals("5:00 PM", response.get("activeSlot"));
        assertEquals(List.of("Evening Hero"), slots.get("5:00 PM").stream().map(slot -> slot.get("name")).toList());
        assertTrue(slots.get("9:00 AM").isEmpty());
        assertTrue(slots.get("9:00 PM").isEmpty());
    }

    @Test
    void beforeFivePm_fivePmSlotRemainsHidden() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-01T12:00:00+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertEquals("9:00 AM", response.get("activeSlot"));
        assertEquals(List.of("Morning Star"), slots.get("9:00 AM").stream().map(slot -> slot.get("name")).toList());
        assertTrue(slots.get("5:00 PM").isEmpty());
        assertTrue(slots.get("9:00 PM").isEmpty());
    }

    @Test
    void beforeNinePm_nightSlotRemainsHidden() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-01T20:59:59+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertEquals("5:00 PM", response.get("activeSlot"));
        assertEquals(List.of("Evening Hero"), slots.get("5:00 PM").stream().map(slot -> slot.get("name")).toList());
        assertTrue(slots.get("9:00 PM").isEmpty());
    }

    @Test
    void atNinePm_switchesToNightSlot() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-01T21:00:00+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertEquals("9:00 PM", response.get("activeSlot"));
        assertEquals(List.of("Night Champion"), slots.get("9:00 PM").stream().map(slot -> slot.get("name")).toList());
        assertTrue(slots.get("9:00 AM").isEmpty());
        assertTrue(slots.get("5:00 PM").isEmpty());
    }

    @Test
    void overnightNightSlot_remainsPublicUntilEightAm() {
        DonorSlotRepository donors = mock(DonorSlotRepository.class);
        stubDonors(donors);
        PublicController controller = controllerAt("2026-06-02T01:00:00+05:30", donors);

        Map<String, Object> response = controller.home();
        Map<String, List<Map<String, Object>>> slots = castSlots(response.get("donorSlots"));

        assertEquals("9:00 PM", response.get("activeSlot"));
        assertEquals(List.of("Night Champion"), slots.get("9:00 PM").stream().map(slot -> slot.get("name")).toList());
        assertTrue(slots.get("9:00 AM").isEmpty());
        assertTrue(slots.get("5:00 PM").isEmpty());
    }

    @Test
    void publicRoomsAreVisibleFromTheirStartUntilTheirEnd() {
        Instant now = Instant.parse("2026-10-14T11:30:00Z");
        Clock clock = Clock.fixed(now, IST);
        CustomRoom active = new CustomRoom("active-secret", "active-password",
                now, now.plusSeconds(3600), now.minusSeconds(120));
        CustomRoomRepository rooms = mock(CustomRoomRepository.class);
        when(rooms.findByStartAtLessThanEqualAndEndAtGreaterThanOrderByPublishedAtDesc(now, now))
                .thenReturn(List.of(active));
        PublicController controller = controllerAt(clock, rooms, mock(DonorSlotRepository.class));

        List<Map<String, Object>> result = controller.publicRooms();

        assertEquals(1, result.size());
        assertEquals("Starting", result.get(0).get("status"));
        assertNull(result.get(0).get("roomCode"));
        assertNull(result.get(0).get("passwordCode"));
        assertEquals(false, result.get(0).get("canCopy"));
    }

    @Test
    void publicRoomsDoNotIncludeRoomsBeforeTheirStartTime() {
        Instant now = Instant.parse("2026-10-14T11:30:00Z");
        Clock clock = Clock.fixed(now, IST);
        CustomRoomRepository rooms = mock(CustomRoomRepository.class);
        when(rooms.findByStartAtLessThanEqualAndEndAtGreaterThanOrderByPublishedAtDesc(now, now))
                .thenReturn(List.of());
        PublicController controller = controllerAt(clock, rooms, mock(DonorSlotRepository.class));

        assertTrue(controller.publicRooms().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Map<String, Object>>> castSlots(Object value) {
        return (Map<String, List<Map<String, Object>>>) value;
    }

    private PublicController controllerAt(String iso, DonorSlotRepository donors) {
        Instant instant = ZonedDateTime.parse(iso).toInstant();
        Clock clock = Clock.fixed(instant, IST);
        return controllerAt(clock, mock(CustomRoomRepository.class), donors);
    }

    private PublicController controllerAt(Clock clock, CustomRoomRepository rooms, DonorSlotRepository donors) {
        RedeemCodeRepository codes = mock(RedeemCodeRepository.class);
        AppSettingRepository settings = mock(AppSettingRepository.class);
        DonationRepository donations = mock(DonationRepository.class);
        FileStorageService storage = mock(FileStorageService.class);
        when(settings.findById(anyString())).thenReturn(Optional.empty());
        PublicController controller = new PublicController(donors, codes, rooms, settings, donations, storage,
                new CustomRoomSchedule(clock), mock(ScheduledBannerService.class), clock);
        return controller;
    }

    private void stubDonors(DonorSlotRepository donors) {
        when(donors.findBySlotNameOrderByIdAsc("9:00 AM")).thenReturn(List.of(new DonorSlot("9:00 AM", "1st", "Morning Star", "₹500", "YouTube", "https://example.com/morning")));
        when(donors.findBySlotNameOrderByIdAsc("5:00 PM")).thenReturn(List.of(new DonorSlot("5:00 PM", "1st", "Evening Hero", "₹700", "Instagram", "https://example.com/evening")));
        when(donors.findBySlotNameOrderByIdAsc("9:00 PM")).thenReturn(List.of(new DonorSlot("9:00 PM", "1st", "Night Champion", "₹900", "Telegram", "https://example.com/night")));
    }
}
