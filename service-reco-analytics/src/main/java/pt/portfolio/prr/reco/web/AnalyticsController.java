package pt.portfolio.prr.reco.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.portfolio.prr.reco.client.FaturacaoClient;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final FaturacaoClient faturacaoClient;

    public AnalyticsController(FaturacaoClient faturacaoClient) {
        this.faturacaoClient = faturacaoClient;
    }

    @GetMapping("/resumo")
    public Map<String, Object> getResumo() {
        var faturas = faturacaoClient.listarFaturas();

        BigDecimal totalFaturado = faturas.stream()
                .map(FaturacaoClient.FaturaDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalSessoesFaturadas", faturas.size(),
                "faturacaoTotal", totalFaturado,
                "moeda", "EUR");
    }
}
