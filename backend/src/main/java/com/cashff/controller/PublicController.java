package com.cashff.controller;

import com.cashff.dto.DonorInput;
import com.cashff.entity.*;
import com.cashff.repository.*;
import com.cashff.service.FileStorageService;
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
 private final DonorSlotRepository donors; private final RedeemCodeRepository codes; private final CustomRoomRepository rooms; private final AppSettingRepository settings; private final DonationRepository donations; private final FileStorageService storage;
 public PublicController(DonorSlotRepository donors,RedeemCodeRepository codes,CustomRoomRepository rooms,AppSettingRepository settings,DonationRepository donations,FileStorageService storage){this.donors=donors;this.codes=codes;this.rooms=rooms;this.settings=settings;this.donations=donations;this.storage=storage;}
 @GetMapping("/home") public Map<String,Object> home(){Map<String,List<Map<String,Object>>> slotMap=new LinkedHashMap<>();for(String slot:List.of("9:00 AM","5:00 PM","9:00 PM")){List<Map<String,Object>> list=donors.findBySlotNameOrderByIdAsc(slot).stream().map(this::donorMap).toList();slotMap.put(slot,list);}return Map.of("donorSlots",slotMap,"bannerUrl",setting("bannerUrl"));}
 @GetMapping("/redeem-codes") public List<Map<String,Object>> redeemCodes(){Instant now=Instant.now();return codes.findAllByOrderByCreatedAtDesc().stream().filter(code -> code.getStartAt()!=null && code.getEndAt()!=null && code.getEndAt().isAfter(code.getStartAt())).map(code -> {String status=code.getStatusAt(now);boolean canCopy="Active".equals(status);Map<String,Object> item=new LinkedHashMap<>();item.put("id",code.getId());item.put("code",code.getCode());item.put("startAt",code.getStartAt().toString());item.put("endAt",code.getEndAt().toString());item.put("status",status);item.put("canCopy",canCopy);item.put("displayStart",formatIst(code.getStartAt()));item.put("displayEnd",formatIst(code.getEndAt()));item.put("createdAt",code.getCreatedAt().toString());return item;}).toList();}
 @GetMapping("/rooms") public List<Map<String,Object>> publicRooms(){Instant now=Instant.now();return rooms.findAllByOrderByPublishedAtDesc().stream().map(r->{boolean active=!now.isBefore(r.getUnlockAt());long seconds=Math.max(0,Duration.between(now,r.getUnlockAt()).getSeconds());List<String> photos=new ArrayList<>();if(r.getFirstPhoto()!=null&&!r.getFirstPhoto().isBlank())photos.add(r.getFirstPhoto());if(r.getSecondPhoto()!=null&&!r.getSecondPhoto().isBlank())photos.add(r.getSecondPhoto());if(r.getThirdPhoto()!=null&&!r.getThirdPhoto().isBlank())photos.add(r.getThirdPhoto());Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getId());m.put("roomCode",active?r.getRoomCode():null);m.put("passwordCode",r.getPasswordCode());m.put("slotTime",r.getSlotTime());m.put("unlockAt",r.getUnlockAt().toString());m.put("secondsRemaining",seconds);m.put("active",active);m.put("photos",photos);return m;}).toList();}
 @GetMapping("/support/qr") public Map<String,String> qr(){return Map.of("qrUrl",setting("merchantQrUrl"));}
 @GetMapping("/banner") public Map<String,String> banner(){return Map.of("bannerUrl",setting("bannerUrl"));}
 @PostMapping(value="/support/donations",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @Transactional
 public Map<String,Object> submitDonation(@RequestParam("whatsapp") @Pattern(regexp="[0-9]{10}") String whatsapp,@RequestParam("amount") @DecimalMin("1.00") @DecimalMax("9999999999.99") BigDecimal amount,@RequestParam("screenshot") MultipartFile screenshot,@RequestParam(name="agree",defaultValue="false") boolean agree){if(!agree)throw new IllegalArgumentException("Please accept the non-refundable donation policy.");if(amount.scale()>2)amount=amount.setScale(2,java.math.RoundingMode.HALF_UP);String url=storage.storeImage(screenshot);Donation donation=donations.findByWhatsapp(whatsapp).orElseGet(()->{Donation d=new Donation();d.setWhatsapp(whatsapp);d.setTotalAmount(BigDecimal.ZERO);return d;});donation.setTotalAmount(donation.getTotalAmount().add(amount));donation.setUpdatedAt(Instant.now());donation.addScreenshot(new DonationScreenshot(url));Donation saved=donations.save(donation);return Map.of("message","Donation submitted successfully.","whatsapp",saved.getWhatsapp(),"totalAmount",saved.getTotalAmount(),"screenshots",saved.getScreenshots().size());}
 private String setting(String key){return settings.findById(key).map(AppSetting::getSettingValue).orElse("");}
 private Map<String,Object> donorMap(DonorSlot d){return Map.of("rank",d.getRankLabel(),"name",d.getSupporterName(),"amount",d.getAmount(),"type",d.getType(),"link",d.getLink());}
 private String formatIst(Instant value){return value.atZone(IST).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a 'IST'"));}
}
