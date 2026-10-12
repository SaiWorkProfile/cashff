package com.cashff.repository;
import com.cashff.entity.AppSetting; import org.springframework.data.jpa.repository.JpaRepository;
public interface AppSettingRepository extends JpaRepository<AppSetting,String>{}
