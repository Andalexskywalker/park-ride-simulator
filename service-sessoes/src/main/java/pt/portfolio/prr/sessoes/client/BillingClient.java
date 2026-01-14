package pt.portfolio.prr.sessoes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "service-tarifas-faturacao", path = "/tarifas/api/faturas")
public interface BillingClient {

    @PostMapping
    void criarFatura(@RequestBody FaturaRequest request);

    record FaturaRequest(Long sessaoId, String matricula, BigDecimal valor) {
    }
}
