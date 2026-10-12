package com.cashff.repository;
import com.cashff.entity.RedeemCode; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface RedeemCodeRepository extends JpaRepository<RedeemCode,Long>{List<RedeemCode> findAllByOrderByCreatedAtDesc();}
