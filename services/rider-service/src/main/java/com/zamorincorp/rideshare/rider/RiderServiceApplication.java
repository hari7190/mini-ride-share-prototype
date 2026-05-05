package com.zamorincorp.rideshare.rider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// entry point for the Rider Service application
@SpringBootApplication
public class RiderServiceApplication {
    public static void main (String[] args){
        SpringApplication.run(RiderServiceApplication.class, args);
    }
}
