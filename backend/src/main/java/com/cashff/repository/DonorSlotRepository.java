package com.cashff.repository;
import com.cashff.entity.DonorSlot; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface DonorSlotRepository extends JpaRepository<DonorSlot,Long>{List<DonorSlot> findBySlotNameOrderByIdAsc(String slotName); List<DonorSlot> findAllByOrderBySlotNameAscIdAsc(); void deleteBySlotName(String slotName);}
