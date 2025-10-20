package pt.portfolio.prr.parques.web;

import pt.portfolio.prr.parques.domain.EstadoParque;
import pt.portfolio.prr.parques.domain.Parque;
import pt.portfolio.prr.parques.repo.ParqueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parques")
@Tag(name = "Parques", description = "CRUD de parques P+R")
public class ParquesController {
    private final ParqueRepository repo;

    public ParquesController(ParqueRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Operation(summary = "Lista todos os parques", description = "Opcional: ?cidade=Lisboa")
    public List<Parque> list(@RequestParam(value = "cidade", required = false) String cidade) {
        if (cidade != null && !cidade.isBlank()) {
            return repo.findByCidadeIgnoreCase(cidade);
        }
        return repo.findAll();
    }

    @GetMapping("/<built-in function id>")
    @Operation(summary = "Obtém um parque por ID")
    public Parque get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Parque não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um parque")
    public Parque create(@RequestBody Parque p) {
        p.setId(null);
        if (p.getOcupacaoAtual() == null) p.setOcupacaoAtual(0);
        if (p.getEstado() == null) p.setEstado(EstadoParque.ABERTO);
        return repo.save(p);
    }

    @PatchMapping("/<built-in function id>/estado")
    @Operation(summary = "Atualiza estado (ABERTO/FECHADO/MANUTENCAO)")
    public Parque updateEstado(@PathVariable Long id, @RequestParam EstadoParque estado) {
        Parque p = repo.findById(id).orElseThrow(() -> new RuntimeException("Parque não encontrado"));
        p.setEstado(estado);
        return repo.save(p);
    }

    @DeleteMapping("/<built-in function id>")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Apaga um parque")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}