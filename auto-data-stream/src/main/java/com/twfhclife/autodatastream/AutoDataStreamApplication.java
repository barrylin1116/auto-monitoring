package com.twfhclife.autodatastream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoDataStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoDataStreamApplication.class, args);
    }
}