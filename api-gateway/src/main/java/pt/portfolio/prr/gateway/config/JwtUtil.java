package pt.portfolio.prr.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // NOTA: Num sistema real, esta chave deve ser partilhada via config server ou
    // vault.
    // Por agora, vou usar uma chave fixa para demonstração.
    private final String SECRET = "EstaChaveTemDeTerPeloMenos32CaracteresParaHS256!!!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}
