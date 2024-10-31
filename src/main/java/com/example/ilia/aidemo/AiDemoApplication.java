package com.example.ilia.aidemo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDemoApplication.class, args);

    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
