package pt.portfolio.prr.parques.web;

import pt.portfolio.prr.parques.domain.EstadoParque;
import pt.portfolio.prr.parques.domain.Parque;
import pt.portfolio.prr.parques.repo.ParqueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um parque por ID")
    public Parque get(@PathVariable("id") Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado"));
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

    // --- PATCH via query param ---
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Atualiza estado (ABERTO/FECHADO/MANUTENCAO)")
    public Parque updateEstadoQuery(
            @PathVariable("id") Long id,
            @RequestParam(name = "estado") String estadoParam) {
        final EstadoParque novo;
        try {
            novo = EstadoParque.valueOf(estadoParam.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estadoParam);
        }

        Parque p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado"));
        p.setEstado(novo);
        return repo.save(p);
    }

    // --- PATCH via JSON ---
    public static record EstadoDTO(String estado) {}
    @PatchMapping(path = "/{id}/estado", consumes = "application/json")
    @Operation(summary = "Atualiza estado via JSON")
    public Parque updateEstadoBody(
            @PathVariable("id") Long id,
            @RequestBody EstadoDTO body) {
        if (body == null || body.estado() == null || body.estado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo 'estado' é obrigatório");
        }
        return updateEstadoQuery(id, body.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Apaga um parque")
    public void delete(@PathVariable("id") Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado");
        }
        repo.deleteById(id);
    }
}
