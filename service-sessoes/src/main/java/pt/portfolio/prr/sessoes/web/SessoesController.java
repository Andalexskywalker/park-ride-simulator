package pt.portfolio.prr.sessoes.web;

import pt.portfolio.prr.sessoes.client.ParquesClient;
import pt.portfolio.prr.sessoes.domain.Sessao;
import pt.portfolio.prr.sessoes.repo.SessaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/sessoes")
public class SessoesController {
  private final SessaoRepository repo;
  private final ParquesClient parques;

  public SessoesController(SessaoRepository repo, ParquesClient parques) {
    this.repo = repo; this.parques = parques;
  }

 public record StartReq(
      @NotBlank @Pattern(regexp="^[A-Z0-9\\-]{5,10}$") String matricula,
      @NotNull Long parqueId) {}

  @PostMapping("/start")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String,Object> start(@Valid @RequestBody StartReq req) {
    // normaliza matrícula
    String mat = req.matricula().toUpperCase();

    // 1) já tem ativa?
    if (repo.findFirstByMatriculaAndEstado(mat, Sessao.Estado.ATIVA).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já tem sessão ativa");

    // 2) parque aberto?
    var parque = parques.get(req.parqueId());
    if (parque == null || !"ABERTO".equalsIgnoreCase(parque.estado()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Parque não está ABERTO");

    // 3) aplica checkin (consome vaga)
    try { parques.checkin(req.parqueId()); }
    catch (Exception e){ throw new ResponseStatusException(HttpStatus.CONFLICT, "Sem vagas no parque"); }

    // 4) cria sessão
    var s = new Sessao();
    s.setParqueId(req.parqueId());
    s.setMatricula(mat);
    s.setInicio(java.time.Instant.now());
    s = repo.save(s);

    return Map.of("sessaoId", s.getId(), "parqueId", s.getParqueId(), "matricula", s.getMatricula(), "inicio", s.getInicio());
  }

  @PostMapping("/{id}/stop")
  public Map<String,Object> stop(@PathVariable("id") Long id) {
    var s = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));
    if (s.getFim()!=null) return Map.of("sessaoId", s.getId(), "status", "já terminada");
    s.setFim(Instant.now());
    s.setMinutos((int) Duration.between(s.getInicio(), s.getFim()).toMinutes());
    s.setEstado(Sessao.Estado.TERMINADA);
    repo.save(s);
    try { parques.checkout(s.getParqueId()); } catch (Exception ignore) {}
    // Preço provisório: 1€/h arredondado ao minuto
    var parque = parques.get(s.getParqueId());
    var precoHora = parque.precoHora() == null ? java.math.BigDecimal.ZERO : parque.precoHora();
    var precoPorMin = precoHora.divide(new java.math.BigDecimal("60"), java.math.RoundingMode.HALF_UP);
    var total = precoPorMin.multiply(new java.math.BigDecimal(s.getMinutos()))
                       .setScale(2, java.math.RoundingMode.HALF_UP);
    return java.util.Map.of(
      "sessaoId", s.getId(), 
      "minutos", s.getMinutos(),
      "total", total
    );

  }
}
