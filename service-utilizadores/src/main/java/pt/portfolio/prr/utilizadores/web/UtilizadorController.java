package pt.portfolio.prr.utilizadores.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pt.portfolio.prr.utilizadores.domain.Utilizador;
import pt.portfolio.prr.utilizadores.domain.Viatura;
import pt.portfolio.prr.utilizadores.repo.UtilizadorRepository;
import pt.portfolio.prr.utilizadores.repo.ViaturaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/utilizadores")
public class UtilizadorController {

    private final UtilizadorRepository utilizadorRepo;
    private final ViaturaRepository viaturaRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private final pt.portfolio.prr.utilizadores.config.JwtUtil jwtUtil;

    public UtilizadorController(UtilizadorRepository utilizadorRepo,
            ViaturaRepository viaturaRepo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            pt.portfolio.prr.utilizadores.config.JwtUtil jwtUtil) {
        this.utilizadorRepo = utilizadorRepo;
        this.viaturaRepo = viaturaRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Utilizador> listarTodos() {
        return utilizadorRepo.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilizador criar(@Valid @RequestBody Utilizador utilizador) {
        if (utilizadorRepo.findByEmail(utilizador.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já registado");
        }

        // Validação de Password Forte
        String pwd = utilizador.getPassword();
        if (pwd == null || !pwd.matches("^(?=.*[A-Z])(?=.*[0-9!@#$%^&*]).{8,}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password fraca. Requer: 8 chars, 1 Maiúscula, 1 Número/Símbolo");
        }

        utilizador.setPassword(passwordEncoder.encode(utilizador.getPassword()));
        return utilizadorRepo.save(utilizador);
    }

    @GetMapping("/{id}")
    public Utilizador obter(@PathVariable("id") Long id) {
        return utilizadorRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado"));
    }

    @PostMapping("/{id}/viaturas")
    @ResponseStatus(HttpStatus.CREATED)
    public Viatura adicionarViatura(@PathVariable("id") Long id, @Valid @RequestBody Viatura viatura) {
        Utilizador user = utilizadorRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado"));

        if (viaturaRepo.findByMatricula(viatura.getMatricula()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já registada");
        }

        viatura.setUtilizador(user);
        return viaturaRepo.save(viatura);
    }

    @GetMapping("/viaturas/{matricula}")
    public Viatura obterViatura(@PathVariable("matricula") String matricula) {
        return viaturaRepo.findByMatricula(matricula.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viatura não encontrada"));
    }

    @GetMapping("/search/matricula/{matricula}")
    public java.util.Map<String, String> searchByMatricula(@PathVariable("matricula") String matricula) {
        Viatura v = viaturaRepo.findByMatricula(matricula.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viatura não encontrada"));

        Utilizador u = v.getUtilizador();
        return java.util.Map.of("nome", u.getNome(), "telemovel", u.getTelemovel() != null ? u.getTelemovel() : "N/A");
    }

    @PostMapping("/operator")
    @ResponseStatus(HttpStatus.CREATED)
    public Utilizador createOperator(@RequestHeader("Authorization") String token,
            @RequestBody Utilizador utilizador) {
        if (!isAdmin(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem criar operadores");
        }

        if (utilizadorRepo.findByEmail(utilizador.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já registado");
        }

        utilizador.setPassword(passwordEncoder.encode(utilizador.getPassword()));
        utilizador.setRole(pt.portfolio.prr.utilizadores.domain.Role.OPERADOR);
        return utilizadorRepo.save(utilizador);
    }

    private boolean isAdmin(String tokenHeader) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(jwtUtil.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "ADMIN".equals(claims.get("role"));
        } catch (Exception e) {
            return false;
        }
    }
}
