package pt.portfolio.prr.sessoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

@EnableFeignClients
public class SessoesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SessoesServiceApplication.class, args);
    }
}