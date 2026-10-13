package com.cashff.controller;

import com.cashff.dto.BulkCodesInput;
import com.cashff.entity.RedeemCode;
import com.cashff.repository.AppSettingRepository;
import com.cashff.repository.CustomRoomRepository;
import com.cashff.repository.DonationRepository;
import com.cashff.repository.DonorSlotRepository;
import com.cashff.repository.RedeemCodeRepository;
import com.cashff.service.CustomRoomSchedule;
import com.cashff.service.FileStorageService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminRedeemCodePublishingTest {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Test
    void publishesCodesUsingTheIstWallClockValues() {
        RedeemCodeRepository repository = mock(RedeemCodeRepository.class);
        when(repository.save(any(RedeemCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AdminController controller = controller(repository);

        Map<String, Object> response = controller.publishCodes(
                new BulkCodesInput("CODE-ONE\nCODE-TWO", "2099-01-01T10:00", "2099-01-01T11:00"));

        assertEquals("Redeem codes published.", response.get("message"));
        assertEquals(2, response.get("count"));
        ArgumentCaptor<RedeemCode> savedCodes = ArgumentCaptor.forClass(RedeemCode.class);
        verify(repository, times(2)).save(savedCodes.capture());
        assertEquals(Instant.parse("2099-01-01T04:30:00Z"), savedCodes.getAllValues().get(0).getStartAt());
        assertEquals(Instant.parse("2099-01-01T05:30:00Z"), savedCodes.getAllValues().get(0).getEndAt());
        assertEquals("2099-01-01T04:30:00Z -> 2099-01-01T05:30:00Z",
                savedCodes.getAllValues().get(0).getScheduledTime());
    }

    @Test
    void rejectsStartTimeInThePastWithoutSavingCodes() {
        RedeemCodeRepository repository = mock(RedeemCodeRepository.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller(repository).publishCodes(
                        new BulkCodesInput("CODE-ONE", "2000-01-01T10:00", "2000-01-01T11:00")));

        assertEquals("Start time cannot be earlier than the current IST time.", exception.getMessage());
        verify(repository, never()).save(any(RedeemCode.class));
    }

    @Test
    void rejectsEndTimeThatIsNotLaterThanStartWithoutSavingCodes() {
        RedeemCodeRepository repository = mock(RedeemCodeRepository.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller(repository).publishCodes(
                        new BulkCodesInput("CODE-ONE", "2099-01-01T11:00", "2099-01-01T11:00")));

        assertEquals("End time must be greater than the start time.", exception.getMessage());
        verify(repository, never()).save(any(RedeemCode.class));
    }

    private AdminController controller(RedeemCodeRepository codes) {
        return new AdminController(mock(DonorSlotRepository.class), codes,
                mock(CustomRoomRepository.class), mock(AppSettingRepository.class),
                mock(DonationRepository.class), mock(FileStorageService.class),
                new CustomRoomSchedule(Clock.system(IST)));
    }
}
