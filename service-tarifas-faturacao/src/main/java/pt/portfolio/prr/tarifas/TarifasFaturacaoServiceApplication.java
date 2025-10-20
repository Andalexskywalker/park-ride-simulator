package pt.portfolio.prr.tarifas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

@EnableFeignClients
public class TarifasFaturacaoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TarifasFaturacaoServiceApplication.class, args);
    }
}