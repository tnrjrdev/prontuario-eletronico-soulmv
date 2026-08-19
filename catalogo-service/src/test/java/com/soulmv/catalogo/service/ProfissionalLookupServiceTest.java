package com.soulmv.catalogo.service;

import com.soulmv.catalogo.client.IamClient;
import com.soulmv.catalogo.client.ProfissionalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * ProfissionalLookupService é o único ponto do projeto protegido por circuit
 * breaker (Resilience4j). A anotação @CircuitBreaker em si não é testada aqui
 * (é responsabilidade do framework); o que se testa é a lógica do método
 * principal e do método de fallback isoladamente, incluindo o caso em que o
 * Feign lança exceção e o fallback deve degradar graciosamente para lista vazia.
 */
@ExtendWith(MockitoExtension.class)
class ProfissionalLookupServiceTest {

    @Mock
    IamClient iamClient;

    @InjectMocks
    ProfissionalLookupService service;

    @Test
    void listarProfissionais_deveRetornarListaDoClienteFeign_quandoSucesso() {
        List<ProfissionalDto> esperado = List.of(
                new ProfissionalDto(1L, "Dra. Ana", Set.of("MEDICO")),
                new ProfissionalDto(2L, "Enf. Carlos", Set.of("ENFERMEIRO")));
        when(iamClient.listarProfissionais()).thenReturn(esperado);

        List<ProfissionalDto> resultado = service.listarProfissionais();

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void listarProfissionais_devePropagarExcecao_quandoFeignFalha() {
        when(iamClient.listarProfissionais()).thenThrow(new RuntimeException("iam-service indisponível"));

        // O método anotado com @CircuitBreaker não trata a exceção sozinho -- é o
        // Resilience4j (via proxy/aspect) quem intercepta a falha e desvia para o
        // método de fallback em runtime. Chamando o método java diretamente (sem
        // proxy), a exceção deve propagar normalmente.
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.listarProfissionais())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("iam-service indisponível");
    }

    @Test
    void fallbackProfissionais_deveRetornarListaVazia_quandoInvocadoComQualquerThrowable() throws Exception {
        Method fallback = ProfissionalLookupService.class
                .getDeclaredMethod("fallbackProfissionais", Throwable.class);
        fallback.setAccessible(true);

        Object resultado = fallback.invoke(service, new RuntimeException("timeout no iam-service"));

        assertThat(resultado).isInstanceOf(List.class);
        assertThat((List<?>) resultado).isEmpty();
    }

    @Test
    void fallbackProfissionais_deveRetornarListaVazia_quandoCausaEhErroDeConexao() throws Exception {
        Method fallback = ProfissionalLookupService.class
                .getDeclaredMethod("fallbackProfissionais", Throwable.class);
        fallback.setAccessible(true);

        Object resultado = fallback.invoke(service, new java.net.ConnectException("connection refused"));

        assertThat((List<?>) resultado).isEmpty();
    }
}
