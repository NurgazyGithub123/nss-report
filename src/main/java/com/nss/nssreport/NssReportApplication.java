package com.nss.nssreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class NssReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(NssReportApplication.class, args);
    }

}
