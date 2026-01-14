package pt.portfolio.prr.sessoes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-utilizadores", path = "/utilizadores/api/utilizadores")
public interface UtilizadoresClient {

    @GetMapping("/viaturas/{matricula}")
    ViaturaDTO getViatura(@PathVariable("matricula") String matricula);

    record ViaturaDTO(Long id, String matricula, String marca, String modelo) {
    }
}
