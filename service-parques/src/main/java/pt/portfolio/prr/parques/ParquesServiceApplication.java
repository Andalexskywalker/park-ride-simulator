package pt.portfolio.prr.parques;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

@EnableFeignClients
public class ParquesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParquesServiceApplication.class, args);
    }
}