package com.socialnetwork.mailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.socialnetwork.mailservice")
public class MailserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailserviceApplication.class, args);
    }

}
