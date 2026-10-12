package com.cashff.repository;
import com.cashff.entity.CustomRoom; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface CustomRoomRepository extends JpaRepository<CustomRoom,Long>{List<CustomRoom> findAllByOrderByPublishedAtDesc();}
