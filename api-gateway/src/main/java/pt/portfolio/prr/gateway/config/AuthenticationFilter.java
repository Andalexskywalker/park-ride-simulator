package pt.portfolio.prr.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import org.springframework.lang.NonNull;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    public static class Config {
    }

    @Override
    @NonNull
    @SuppressWarnings("null")
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (request.getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Auth Header"));
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token Format"));
            }

            String token = authHeader.substring(7);

            System.out.println("GATEWAY: Intercepting " + exchange.getRequest().getPath());
            Map<String, String> body = Map.of("token", token);
            return webClientBuilder.build()
                    .post()
                    .uri("http://service-utilizadores:8082/utilizadores/api/auth/validate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorResume(e -> {
                        System.out.println("GATEWAY: Error calling auth service: " + e.getMessage());
                        // e.printStackTrace();
                        // Don't print stack trace for every connection error to avoid spam, just
                        // message
                        return Mono.error(
                                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Auth Service Unreachable"));
                    })
                    .flatMap(isValid -> {
                        System.out.println(
                                "GATEWAY: Validation result for " + exchange.getRequest().getPath() + ": " + isValid);
                        if (isValid) {
                            return chain.filter(exchange);
                        } else {
                            System.out.println("GATEWAY: Token Invalid/Expired. Returning 401.");
                            return Mono.error(
                                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid/Expired Token"));
                        }
                    });
        };
    }
}
