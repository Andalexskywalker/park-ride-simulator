package pt.portfolio.prr.tarifas.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InfoController {
    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
            "service", appName,
            "timestamp", OffsetDateTime.now().toString(),
            "status", "ok"
        );
    }
}