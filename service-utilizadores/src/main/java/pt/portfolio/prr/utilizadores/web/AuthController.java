package pt.portfolio.prr.utilizadores.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pt.portfolio.prr.utilizadores.config.JwtUtil;
import pt.portfolio.prr.utilizadores.domain.Utilizador;
import pt.portfolio.prr.utilizadores.repo.UtilizadorRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilizadorRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UtilizadorRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        Utilizador user = repo.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        String sessionId = java.util.UUID.randomUUID().toString();
        user.setLastSessionId(sessionId);
        repo.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), sessionId);
        return Map.of(
                "token", token,
                "id", String.valueOf(user.getId()),
                "nome", user.getNome(),
                "email", user.getEmail(),
                "role", user.getRole().name());
    }

    @PostMapping("/validate")
    public boolean validate(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        System.out.println("DEBUG VALIDATE: Processing token...");
        try {
            if (jwtUtil.isExpired(token)) {
                System.out.println("DEBUG VALIDATE: Token expired via jwtUtil");
                return false;
            }

            // Extrair Session ID do token
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(jwtUtil.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenSid = (String) claims.get("sid");
            String email = claims.getSubject();

            System.out.println("DEBUG VALIDATE: Email=" + email + " SID=" + tokenSid);

            if (tokenSid == null) {
                System.out.println("DEBUG VALIDATE: tokenSid is null");
                return false;
            }

            // Validar com a BD
            Utilizador user = repo.findByEmail(email).orElse(null);
            if (user == null) {
                System.out.println("DEBUG VALIDATE: User not found");
                return false;
            }

            System.out.println("DEBUG VALIDATE: DBSID=" + user.getLastSessionId());
            boolean match = tokenSid.equals(user.getLastSessionId());
            System.out.println("DEBUG VALIDATE: Match=" + match);
            return match;

        } catch (Exception e) {
            System.out.println("DEBUG VALIDATE: Exception " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public record LoginRequest(String email, String password) {
    }
}
