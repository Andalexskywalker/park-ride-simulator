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
  private final pt.portfolio.prr.sessoes.client.UtilizadoresClient utilizadores;
  private final pt.portfolio.prr.sessoes.client.BillingClient billing;

  public SessoesController(SessaoRepository repo,
      ParquesClient parques,
      pt.portfolio.prr.sessoes.client.UtilizadoresClient utilizadores,
      pt.portfolio.prr.sessoes.client.BillingClient billing) {
    this.repo = repo;
    this.parques = parques;
    this.utilizadores = utilizadores;
    this.billing = billing;
  }

  public record StartReq(
      @NotBlank String matricula,
      @NotNull Long parqueId) {
  }

  @PostMapping("/start")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> start(@Valid @RequestBody StartReq req) {
    String mat = req.matricula().toUpperCase();
    System.out.println("DEBUG: Iniciando sessão para " + mat + " no parque " + req.parqueId());

    // 1) Verificar se a viatura está registada
    try {
      System.out.println("DEBUG: Verificando viatura no service-utilizadores...");
      utilizadores.getViatura(mat);
      System.out.println("DEBUG: Viatura verificada com sucesso.");
    } catch (Exception e) {
      System.out.println("DEBUG: Falha ao verificar viatura: " + e.getMessage());
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viatura não registada no sistema: " + e.getMessage());
    }

    // 2) Já tem sessão ativa?
    if (repo.findFirstByMatriculaAndEstado(mat, Sessao.Estado.ATIVA).isPresent()) {
      System.out.println("DEBUG: Matrícula já tem sessão ativa.");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já tem sessão ativa");
    }

    // 3) Parque aberto?
    var parque = parques.get(req.parqueId());
    if (parque == null || !"ABERTO".equalsIgnoreCase(parque.estado())) {
      System.out.println("DEBUG: Parque fechado ou não encontrado.");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Parque não está ABERTO");
    }

    // 4) Tentar checkin (reduz ocupação)
    try {
      parques.checkin(req.parqueId());
    } catch (Exception e) {
      System.out.println("DEBUG: Sem vagas.");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Sem vagas no parque");
    }

    // 5) Criar sessão
    var s = new Sessao();
    s.setParqueId(req.parqueId());
    s.setMatricula(mat);
    s.setInicio(java.time.Instant.now());
    s.setEstado(Sessao.Estado.ATIVA);
    s = repo.save(s);
    System.out.println("DEBUG: Sessão criada com sucesso ID: " + s.getId());

    return Map.of("sessaoId", s.getId(), "parqueId", s.getParqueId(), "matricula", s.getMatricula(), "inicio",
        s.getInicio());
  }

  @PostMapping("/{id}/stop")
  public Map<String, Object> stop(@PathVariable("id") Long id) {
    var s = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));
    if (s.getFim() != null)
      return Map.of("sessaoId", s.getId(), "status", "já terminada");
    s.setFim(Instant.now());
    s.setMinutos((int) Duration.between(s.getInicio(), s.getFim()).toMinutes());
    s.setEstado(Sessao.Estado.TERMINADA);
    repo.save(s);
    try {
      parques.checkout(s.getParqueId());
    } catch (Exception ignore) {
    }
    // Preço provisório: 1€/h arredondado ao minuto
    var parque = parques.get(s.getParqueId());
    var precoHora = parque.precoHora() == null ? java.math.BigDecimal.ZERO : parque.precoHora();
    var precoPorMin = precoHora.divide(new java.math.BigDecimal("60"), java.math.RoundingMode.HALF_UP);
    var total = precoPorMin.multiply(new java.math.BigDecimal(s.getMinutos()))
        .setScale(2, java.math.RoundingMode.HALF_UP);

    // 6) Enviar para faturação
    try {
      billing.criarFatura(
          new pt.portfolio.prr.sessoes.client.BillingClient.FaturaRequest(s.getId(), s.getMatricula(), total));
    } catch (Exception e) {
      // Ignorar erro de faturação para não bloquear o checkout
    }

    return java.util.Map.of(
        "sessaoId", s.getId(),
        "minutos", s.getMinutos(),
        "total", total);

  }

  @GetMapping("/active/{matricula}")
  public Map<String, Object> getActiveSession(@PathVariable("matricula") String matricula) {
    Sessao s = repo.findFirstByMatriculaAndEstado(matricula, Sessao.Estado.ATIVA)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma sessão ativa"));

    String parkName = "Parque desconhecido";
    try {
      var p = parques.get(s.getParqueId());
      if (p != null)
        parkName = p.nome();
    } catch (Exception e) {
    }

    return Map.of(
        "sessaoId", s.getId(),
        "matricula", s.getMatricula(),
        "parqueId", s.getParqueId(),
        "parkName", parkName,
        "inicio", s.getInicio());
  }

  @GetMapping("/analytics")
  public Map<String, Object> getAnalytics() {
    long totalSessoes = repo.count();
    long ativas = repo.findAll().stream().filter(s -> s.getEstado() == Sessao.Estado.ATIVA).count();

    // Receita total (simplificado, somando minutos * preço médio ou apenas sessões
    // fechadas?)
    double receitaEstimada = (totalSessoes - ativas) * 2.50;

    return Map.of(
        "totalSessoes", totalSessoes,
        "sessoesAtivas", ativas, // Renamed from sessoesAtivas
        "receitaTotal", receitaEstimada);
  }

  @GetMapping("/active/parque/{parqueId}")
  public java.util.List<Map<String, Object>> getActiveByParque(@PathVariable("parqueId") Long parqueId) {
    return repo.findByParqueIdAndEstado(parqueId, Sessao.Estado.ATIVA).stream()
        .map(s -> {
          Map<String, Object> m = new java.util.HashMap<>();
          m.put("sessaoId", s.getId());
          m.put("matricula", s.getMatricula());
          m.put("inicio", s.getInicio());
          return m;
        })
        .collect(java.util.stream.Collectors.toList());
  }
}
