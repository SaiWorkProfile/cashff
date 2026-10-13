package com.cashff.controller;

import com.cashff.entity.*;
import com.cashff.repository.*;
import com.cashff.service.FileStorageService;
import com.cashff.service.CustomRoomSchedule;
import com.cashff.service.ScheduledBannerService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController @Validated @RequestMapping("/api/public")
public class PublicController {
 private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
 private Clock clock;
 private final DonorSlotRepository donors; private final RedeemCodeRepository codes; private final CustomRoomRepository rooms; private final AppSettingRepository settings; private final DonationRepository donations; private final FileStorageService storage; private final CustomRoomSchedule roomSchedule; private final ScheduledBannerService banners;
 public PublicController(DonorSlotRepository donors,RedeemCodeRepository codes,CustomRoomRepository rooms,AppSettingRepository settings,DonationRepository donations,FileStorageService storage,CustomRoomSchedule roomSchedule,ScheduledBannerService banners,Clock clock){this.donors=donors;this.codes=codes;this.rooms=rooms;this.settings=settings;this.donations=donations;this.storage=storage;this.roomSchedule=roomSchedule;this.banners=banners;this.clock=clock;}
 void setClock(Clock clock){this.clock=clock;}
 @GetMapping("/home") public Map<String,Object> home(){Instant now=clock.instant();String activeSlot=activePublicSlot(now);Map<String,List<Map<String,Object>>> slotMap=new LinkedHashMap<>();for(String slot:List.of("9:00 AM","5:00 PM","9:00 PM")){slotMap.put(slot,isSlotPubliclyVisible(slot,now)?donors.findBySlotNameOrderByIdAsc(slot).stream().map(this::donorMap).toList():List.of());}Map<String,Object> response=new LinkedHashMap<>();response.put("donorSlots",slotMap);response.put("activeSlot",activeSlot);response.put("bannerUrl",banners.findActive().map(banner->banner.getImageUrl()).orElseGet(()->setting("bannerUrl")));return response;}
 @GetMapping("/redeem-codes") public List<Map<String,Object>> redeemCodes(){Instant now=clock.instant();return codes.findByStartAtLessThanEqualAndEndAtGreaterThanOrderByCreatedAtDesc(now,now).stream().map(code -> {Map<String,Object> item=new LinkedHashMap<>();item.put("id",code.getId());item.put("code",code.getCode());item.put("startAt",code.getStartAt().toString());item.put("endAt",code.getEndAt().toString());item.put("status","Active");item.put("canCopy",true);item.put("displayStart",formatIst(code.getStartAt()));item.put("displayEnd",formatIst(code.getEndAt()));item.put("createdAt",code.getCreatedAt().toString());return item;}).toList();}
 @GetMapping("/rooms") public List<Map<String,Object>> publicRooms(){Instant now=clock.instant();return rooms.findByStartAtLessThanEqualAndEndAtGreaterThanOrderByPublishedAtDesc(now,now).stream().map(roomSchedule::toPublicResponse).toList();}
 @GetMapping("/support/qr") public Map<String,String> qr(){return Map.of("qrUrl",setting("merchantQrUrl"));}
 @GetMapping("/banner") public Map<String,String> banner(){return Map.of("bannerUrl",banners.findActive().map(banner->banner.getImageUrl()).orElseGet(()->setting("bannerUrl")));}
 @PostMapping(value="/support/donations",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @Transactional
 public Map<String,Object> submitDonation(@RequestParam("whatsapp") @Pattern(regexp="[0-9]{10}") String whatsapp,@RequestParam("amount") @DecimalMin("1.00") @DecimalMax("9999999999.99") BigDecimal amount,@RequestParam("screenshot") MultipartFile screenshot,@RequestParam(name="agree",defaultValue="false") boolean agree){if(!agree)throw new IllegalArgumentException("Please accept the non-refundable donation policy.");if(amount.scale()>2)amount=amount.setScale(2,java.math.RoundingMode.HALF_UP);String url=storage.storeImage(screenshot);Donation donation=donations.findByWhatsapp(whatsapp).orElseGet(()->{Donation d=new Donation();d.setWhatsapp(whatsapp);d.setTotalAmount(BigDecimal.ZERO);return d;});donation.setTotalAmount(donation.getTotalAmount().add(amount));donation.setUpdatedAt(Instant.now());donation.addScreenshot(new DonationScreenshot(url));Donation saved=donations.save(donation);return Map.of("message","Donation submitted successfully.","whatsapp",saved.getWhatsapp(),"totalAmount",saved.getTotalAmount(),"screenshots",saved.getScreenshots().size());}
 private String setting(String key){return settings.findById(key).map(AppSetting::getSettingValue).orElse("");}
 private boolean isSlotPubliclyVisible(String slot,Instant now){LocalTime time=now.atZone(IST).toLocalTime();return switch(slot){case "9:00 AM" -> !time.isBefore(LocalTime.of(9,0)) && time.isBefore(LocalTime.of(17,0)); case "5:00 PM" -> !time.isBefore(LocalTime.of(17,0)) && time.isBefore(LocalTime.of(21,0)); case "9:00 PM" -> !time.isBefore(LocalTime.of(21,0)) || time.isBefore(LocalTime.of(8,0)); default -> false;};}
 private String activePublicSlot(Instant now){LocalTime time=now.atZone(IST).toLocalTime();if(time.isBefore(LocalTime.of(8,0))) return "9:00 PM";if(time.isBefore(LocalTime.of(9,0))) return null;if(time.isBefore(LocalTime.of(17,0))) return "9:00 AM";if(time.isBefore(LocalTime.of(21,0))) return "5:00 PM";return "9:00 PM";}
 private Map<String,Object> donorMap(DonorSlot d){return Map.of("rank",d.getRankLabel(),"name",d.getSupporterName(),"amount",d.getAmount(),"type",d.getType(),"link",d.getLink());}
 private String formatIst(Instant value){return value.atZone(IST).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a 'IST'"));}
}
