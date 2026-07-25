package com.campusbite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CampusBiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusBiteApplication.class, args);
    }
}
