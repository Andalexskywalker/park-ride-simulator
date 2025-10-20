package pt.portfolio.prr.utilizadores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

@EnableFeignClients
public class UtilizadoresServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UtilizadoresServiceApplication.class, args);
    }
}