package com.cashff.controller;

import com.cashff.entity.ScheduledBanner;
import com.cashff.service.ScheduledBannerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/banners")
public class ScheduledBannerAdminController {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a 'IST'");

    private final ScheduledBannerService banners;

    public ScheduledBannerAdminController(ScheduledBannerService banners) {
        this.banners = banners;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return banners.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> schedule(@RequestParam("image") MultipartFile image,
                                        @RequestParam("startAt") String startAt,
                                        @RequestParam("endAt") String endAt) {
        return toResponse(banners.schedule(image, startAt, endAt));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable long id) {
        banners.delete(id);
        return Map.of("message", "Scheduled banner deleted.");
    }

    private Map<String, Object> toResponse(ScheduledBanner banner) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", banner.getId());
        response.put("imageUrl", banner.getImageUrl());
        response.put("startAt", banner.getStartAt().toString());
        response.put("endAt", banner.getEndAt().toString());
        response.put("displayStart", banner.getStartAt().atZone(IST).format(DISPLAY_DATE_TIME));
        response.put("displayEnd", banner.getEndAt().atZone(IST).format(DISPLAY_DATE_TIME));
        return response;
    }
}
