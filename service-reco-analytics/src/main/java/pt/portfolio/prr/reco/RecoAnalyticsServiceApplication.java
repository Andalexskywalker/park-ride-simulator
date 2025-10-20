package pt.portfolio.prr.reco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

@EnableFeignClients
public class RecoAnalyticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecoAnalyticsServiceApplication.class, args);
    }
}