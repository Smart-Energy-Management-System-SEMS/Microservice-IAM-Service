package com.sems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class SemsIamApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemsIamApplication.class, args);
    }

}
