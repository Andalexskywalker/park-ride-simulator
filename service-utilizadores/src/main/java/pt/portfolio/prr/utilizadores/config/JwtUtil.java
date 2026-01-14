package pt.portfolio.prr.utilizadores.config;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final String SECRET = "EstaChaveTemDeTerPeloMenos32CaracteresParaHS256!!!";
    private final javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes());

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    public String generateToken(String email, String role, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("sid", sessionId);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public SecretKey getSecretKey() {
        return key;
    }

    public boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
