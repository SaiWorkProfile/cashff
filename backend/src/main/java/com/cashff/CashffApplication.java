package com.cashff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CashffApplication {
    public static void main(String[] args) { SpringApplication.run(CashffApplication.class, args); }
}
