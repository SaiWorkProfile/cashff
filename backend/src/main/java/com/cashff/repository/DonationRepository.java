package com.cashff.repository;
import com.cashff.entity.Donation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface DonationRepository extends JpaRepository<Donation,Long>{Optional<Donation> findByWhatsapp(String whatsapp);}
