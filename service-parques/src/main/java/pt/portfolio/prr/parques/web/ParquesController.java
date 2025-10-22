package pt.portfolio.prr.parques.web;

import pt.portfolio.prr.parques.domain.EstadoParque;
import pt.portfolio.prr.parques.domain.Parque;
import pt.portfolio.prr.parques.repo.ParqueRepository;
import pt.portfolio.prr.parques.service.ParquesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/parques")
@Tag(name = "Parques", description = "CRUD de parques P+R")
public class ParquesController {
    private final ParqueRepository repo;
    private final ParquesService svc;

    public ParquesController(ParqueRepository repo, ParquesService svc) {
        this.repo = repo;
        this.svc = svc;
    }

    record OcupacaoDTO(Long parqueId, int ocupacaoAtual, int capacidadeTotal) {}
    public static record EstadoDTO(String estado) {}

        
    @GetMapping
    @Operation(summary = "Lista parques, opcionalmente filtrando por cidade")
    public Page<Parque> list(
            @RequestParam(name = "cidade", required = false) String cidade,
            @PageableDefault(size = 20) Pageable pageable) {
        if (cidade != null && !cidade.isBlank()) {
            return repo.findByCidadeIgnoreCase(cidade.trim(), pageable);
        } else {
            return repo.findAll(pageable);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um parque por ID")
    public Parque get(@PathVariable("id") Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado"));
    }

    @PostMapping
    @Operation(summary = "Cria um parque")
    public ResponseEntity<Parque> create(@Valid @RequestBody Parque p, UriComponentsBuilder uri) {
        p.setId(null);
        if (p.getOcupacaoAtual() == null) p.setOcupacaoAtual(0);
        if (p.getEstado() == null) p.setEstado(EstadoParque.ABERTO);
        Parque saved = repo.save(p);
        return ResponseEntity
            .created(uri.path("/api/parques/{id}").buildAndExpand(saved.getId()).toUri())
            .body(saved);
    }

    @PatchMapping(value = "/{id}/estado", params = "estado")
    @Operation(summary = "Atualiza estado (ABERTO/FECHADO/MANUTENCAO)")
    public Parque updateEstadoQuery(
            @PathVariable("id") Long id,
            @RequestParam(name = "estado") String estadoParam) {
        final EstadoParque novo;
        try { novo = EstadoParque.valueOf(estadoParam.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estadoParam);
        }
        var p = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parque não encontrado"));
        p.setEstado(novo);
        return repo.save(p);
    }

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

    @PostMapping("/{id}/checkin")
    public OcupacaoDTO checkin(@PathVariable("id") Long id) {
        var p = svc.checkin(id);
        return new OcupacaoDTO(p.getId(), p.getOcupacaoAtual(), p.getCapacidadeTotal());
    }

    @PostMapping("/{id}/checkout")
    public OcupacaoDTO checkout(@PathVariable("id") Long id) {
        var p = svc.checkout(id);
        return new OcupacaoDTO(p.getId(), p.getOcupacaoAtual(), p.getCapacidadeTotal());
    }

    @GetMapping("/{id}/livres")
    public java.util.Map<String,Object> livres(@PathVariable("id") Long id){
        var p = repo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Parque não encontrado"));
            int livres = p.getCapacidadeTotal() - p.getOcupacaoAtual();
            return java.util.Map.of("parqueId", p.getId(), "livres", livres, "capacidadeTotal", p.getCapacidadeTotal());
    }

}


