package pt.portfolio.prr.sessoes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="service-parques", path="/parques/api/parques")
public interface ParquesClient {
  @PostMapping("/{id}/checkin")  OcupacaoDTO checkin(@PathVariable("id") Long id);
  @PostMapping("/{id}/checkout") OcupacaoDTO checkout(@PathVariable("id") Long id);
  @GetMapping("/{id}") ParqueDTO get(@PathVariable("id") Long id);

  record OcupacaoDTO(Long parqueId, int ocupacaoAtual, int capacidadeTotal) {}
  record ParqueDTO(Long id, String nome, String cidade, Integer capacidadeTotal,
                   Integer ocupacaoAtual, String estado, java.math.BigDecimal precoHora) {}
}

