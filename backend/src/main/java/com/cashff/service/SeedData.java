package com.cashff.service;
import com.cashff.entity.*; import com.cashff.repository.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component;
import java.time.Instant;
@Component public class SeedData implements CommandLineRunner {
 private final AdminUserRepository users; private final PasswordEncoder encoder; private final DonorSlotRepository donors; private final RedeemCodeRepository codes; private final AppSettingRepository settings;
 @Value("${app.admin-username:admin}") private String adminUsername; @Value("${app.admin-password:change-me-now}") private String adminPassword;
 public SeedData(AdminUserRepository users,PasswordEncoder encoder,DonorSlotRepository donors,RedeemCodeRepository codes,AppSettingRepository settings){this.users=users;this.encoder=encoder;this.donors=donors;this.codes=codes;this.settings=settings;}
 @Override public void run(String... args){
  if(users.count()==0){AdminUser u=new AdminUser();u.setUsername(adminUsername);u.setPasswordHash(encoder.encode(adminPassword));u.setRole("ADMIN");users.save(u);}
  if(donors.count()==0){String[] slots={"9:00 AM","5:00 PM","9:00 PM"};String[][] names={{"Rahul Gamer","Amit FF","Sahil Esports"},{"Vicky King","Rohit FF","Aman OP"},{"Deepak Boss","Karan FF","Ravi YT"}};String[][] amounts={{"₹500","₹300","₹150"},{"₹800","₹400","₹200"},{"₹1000","₹600","₹300"}};String[][] types={{"YouTube","Instagram","Telegram"},{"YouTube","Instagram","WhatsApp"},{"YouTube","Instagram","Facebook"}};String[][] links={{"https://youtube.com","https://instagram.com","https://telegram.org"},{"https://youtube.com","https://instagram.com","https://whatsapp.com"},{"https://youtube.com","https://instagram.com","https://facebook.com"}};String[] ranks={"1st","2nd","3rd"};for(int s=0;s<3;s++)for(int i=0;i<3;i++)donors.save(new DonorSlot(slots[s],ranks[i],names[s][i],amounts[s][i],types[s][i],links[s][i]));}
  if(codes.count()==0){Instant now = Instant.now(); codes.save(new RedeemCode("FFMC 2535 78MK 99AZ", now.plusSeconds(3600), now.plusSeconds(7200)));}
  settings.findById("merchantQrUrl").orElseGet(()->settings.save(new AppSetting("merchantQrUrl","")));
  settings.findById("bannerUrl").orElseGet(()->settings.save(new AppSetting("bannerUrl","")));
 }
}
