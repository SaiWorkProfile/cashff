package com.cashff.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DonationRelationshipTest {

    @Test
    void addScreenshotKeepsBothSidesInSync() {
        Donation donation = new Donation();
        DonationScreenshot screenshot = new DonationScreenshot("image.png");

        donation.addScreenshot(screenshot);

        assertSame(donation, screenshot.getDonation());
        assertEquals(List.of(screenshot), donation.getScreenshots());
    }

    @Test
    void changingScreenshotDonationMovesItBetweenCollections() {
        Donation firstDonation = new Donation();
        Donation secondDonation = new Donation();
        DonationScreenshot screenshot = new DonationScreenshot("image.png");

        firstDonation.addScreenshot(screenshot);
        screenshot.setDonation(secondDonation);

        assertTrue(firstDonation.getScreenshots().isEmpty());
        assertSame(secondDonation, screenshot.getDonation());
        assertEquals(List.of(screenshot), secondDonation.getScreenshots());
    }

    @Test
    void replacingScreenshotsUpdatesBothSides() {
        Donation donation = new Donation();
        DonationScreenshot removed = new DonationScreenshot("removed.png");
        DonationScreenshot retained = new DonationScreenshot("retained.png");
        DonationScreenshot added = new DonationScreenshot("added.png");
        donation.addScreenshot(removed);
        donation.addScreenshot(retained);

        donation.setScreenshots(List.of(retained, added));

        assertEquals(List.of(retained, added), donation.getScreenshots());
        assertNull(removed.getDonation());
        assertSame(donation, retained.getDonation());
        assertSame(donation, added.getDonation());
    }
}
