package pt.portfolio.prr.tarifas.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pt.portfolio.prr.tarifas.domain.Fatura;
import pt.portfolio.prr.tarifas.repo.FaturaRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/faturas")
public class FaturaController {

    private final FaturaRepository repo;

    public FaturaController(FaturaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Fatura> listar() {
        return repo.findAll();
    }

    @GetMapping("/matricula/{matricula}")
    public List<Fatura> listarPorMatricula(@PathVariable String matricula) {
        return repo.findByMatricula(matricula);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Fatura criar(@RequestBody FaturaRequest request) {
        Fatura fatura = new Fatura(request.sessaoId(), request.matricula(), request.valor());
        return repo.save(fatura);
    }

    public record FaturaRequest(Long sessaoId, String matricula, BigDecimal valor) {
    }
}
