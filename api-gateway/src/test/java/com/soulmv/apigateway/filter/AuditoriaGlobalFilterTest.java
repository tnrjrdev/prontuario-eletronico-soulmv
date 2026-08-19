package com.soulmv.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuditoriaGlobalFilterTest {

    @Mock WebClient.Builder builder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuditoriaGlobalFilter novoFiltro() {
        return new AuditoriaGlobalFilter(WebClient.builder(), objectMapper, "token-de-teste");
    }

    private String jwtCom(String json) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".assinatura-fake";
    }

    @Test
    void extrairLogin_deveLerClaimSub_deTokenValido() {
        AuditoriaGlobalFilter filtro = novoFiltro();
        ServerHttpRequest request = MockServerHttpRequest.get("/api/pacientes")
                .header("Authorization", "Bearer " + jwtCom("{\"sub\":\"medico1\",\"roles\":[\"MEDICO\"]}"))
                .build();

        assertThat(filtro.extrairLogin(request)).isEqualTo("medico1");
    }

    @Test
    void extrairLogin_deveRetornarAnonimo_quandoSemHeaderAuthorization() {
        AuditoriaGlobalFilter filtro = novoFiltro();
        ServerHttpRequest request = MockServerHttpRequest.get("/api/pacientes").build();

        assertThat(filtro.extrairLogin(request)).isEqualTo("anonimo");
    }

    @Test
    void extrairLogin_deveRetornarAnonimo_quandoTokenMalformado() {
        AuditoriaGlobalFilter filtro = novoFiltro();
        ServerHttpRequest request = MockServerHttpRequest.get("/api/pacientes")
                .header("Authorization", "Bearer nao-e-um-jwt-de-verdade")
                .build();

        assertThat(filtro.extrairLogin(request)).isEqualTo("anonimo");
    }

    @Test
    void extrairLogin_deveRetornarAnonimo_quandoPayloadNaoTemClaimSub() {
        AuditoriaGlobalFilter filtro = novoFiltro();
        ServerHttpRequest request = MockServerHttpRequest.get("/api/pacientes")
                .header("Authorization", "Bearer " + jwtCom("{\"roles\":[\"MEDICO\"]}"))
                .build();

        assertThat(filtro.extrairLogin(request)).isEqualTo("anonimo");
    }

    @Test
    void ipOrigem_devePreferirXForwardedFor_quandoPresente() {
        AuditoriaGlobalFilter filtro = novoFiltro();
        ServerHttpRequest request = MockServerHttpRequest.get("/api/pacientes")
                .header("X-Forwarded-For", "203.0.113.5, 10.0.0.1")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 12345))
                .build();

        assertThat(filtro.ipOrigem(request)).isEqualTo("203.0.113.5");
    }

    @Test
    void ipOrigem_deveUsarRemoteAddress_quandoSemXForwardedFor() {
        AuditoriaGlobalFilter filtro = novoFiltro();
        ServerHttpRequest request = MockServerHttpRequest.get("/api/pacientes")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 12345))
                .build();

        assertThat(filtro.ipOrigem(request)).isEqualTo("127.0.0.1");
    }
}
