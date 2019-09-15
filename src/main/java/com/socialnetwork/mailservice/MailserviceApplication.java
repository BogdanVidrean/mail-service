package com.socialnetwork.mailservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(scanBasePackages = "com.socialnetwork.mailservice")
@EnableScheduling
public class MailserviceApplication {

    public static void main(String[] args) {
        run(MailserviceApplication.class, args);
    }

}
