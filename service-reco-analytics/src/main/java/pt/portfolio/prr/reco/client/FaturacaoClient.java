package pt.portfolio.prr.reco.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@FeignClient(name = "service-tarifas-faturacao", path = "/tarifas/api/faturas")
public interface FaturacaoClient {

    @GetMapping
    List<FaturaDTO> listarFaturas();

    record FaturaDTO(Long id, Long sessaoId, String matricula, BigDecimal valor, Instant dataEmissao) {
    }
}
