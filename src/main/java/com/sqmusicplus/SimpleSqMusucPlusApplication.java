package com.sqmusicplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = FreeMarkerAutoConfiguration.class)
@EnableScheduling
public class SimpleSqMusucPlusApplication {






    public static void main(String[] args) {
        SpringApplication.run(SimpleSqMusucPlusApplication.class, args);
    }

}
