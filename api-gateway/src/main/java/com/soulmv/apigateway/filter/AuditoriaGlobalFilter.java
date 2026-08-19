package com.soulmv.apigateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Registra toda requisição que passa pelo gateway na trilha de auditoria, de forma
 * assíncrona e "fire-and-forget": nunca atrasa nem quebra a resposta real, mesmo que
 * o auditoria-service esteja fora do ar (falha é só logada). É o único produtor da
 * trilha de auditoria dos microsserviços — o monólito continua auditando sozinho via
 * seu próprio AuditoriaInterceptor.
 */
@Component
public class AuditoriaGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaGlobalFilter.class);
    private static final String AUDITORIA_URI = "lb://auditoria-service/api/auditoria/eventos";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String internalToken;

    public AuditoriaGlobalFilter(WebClient.Builder loadBalancedWebClientBuilder,
                                 ObjectMapper objectMapper,
                                 @Value("${app.security.internal-audit-token}") String internalToken) {
        this.webClient = loadBalancedWebClientBuilder.build();
        this.objectMapper = objectMapper;
        this.internalToken = internalToken;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String metodo = request.getMethod() != null ? request.getMethod().name() : "?";
        String caminho = request.getPath().value();
        String ip = ipOrigem(request);
        String usuarioLogin = extrairLogin(request);

        return chain.filter(exchange)
                .doFinally(signal -> registrarAssincrono(usuarioLogin, metodo, caminho, ip, exchange));
    }

    private void registrarAssincrono(String usuarioLogin, String metodo, String caminho, String ip,
                                     ServerWebExchange exchange) {
        int status = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value() : 0;

        webClient.post()
                .uri(AUDITORIA_URI)
                .header("X-Internal-Token", internalToken)
                .bodyValue(Map.of(
                        "usuarioLogin", usuarioLogin,
                        "metodo", metodo,
                        "caminho", caminho,
                        "status", status,
                        "ip", ip))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(e -> {
                    log.warn("Falha ao registrar auditoria para {} {}: {}", metodo, caminho, e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    String ipOrigem(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remote = request.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress() : "desconhecido";
    }

    String extrairLogin(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return "anonimo";
        }
        try {
            String[] partes = auth.substring(7).split("\\.");
            if (partes.length < 2) {
                return "anonimo";
            }
            String payloadBase64 = partes[1];
            int resto = payloadBase64.length() % 4;
            if (resto != 0) {
                payloadBase64 += "=".repeat(4 - resto);
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadBase64);
            JsonNode payload = objectMapper.readTree(new String(payloadBytes, StandardCharsets.UTF_8));
            JsonNode sub = payload.get("sub");
            return sub != null ? sub.asText() : "anonimo";
        } catch (Exception e) {
            // Decodificação é só para fins de log; token malformado nunca deve
            // impedir o registro do evento nem propagar erro para a requisição real.
            return "anonimo";
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
