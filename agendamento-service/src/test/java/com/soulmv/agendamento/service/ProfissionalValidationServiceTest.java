package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.ProfissionalDto;
import com.soulmv.agendamento.client.UsuarioClient;
import com.soulmv.agendamento.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalValidationServiceTest {

    @Mock UsuarioClient client;
    @InjectMocks ProfissionalValidationService service;

    @Test
    void validar_deveRetornarProfissional_quandoEstaNaListaDeAtivosComRoleClinica() {
        ProfissionalDto medico = new ProfissionalDto(2L, "Dr. Fulano", Set.of("MEDICO"));
        when(client.listarProfissionais()).thenReturn(List.of(medico, new ProfissionalDto(3L, "Enf. Ciclana", Set.of("ENFERMEIRO"))));

        assertThat(service.validar(2L)).isEqualTo(medico);
    }

    @Test
    void validar_deveFalhar_quandoIdNaoEstaNaLista() {
        // A lista do iam-service já vem filtrada (só ativos com role clínica), então
        // "não está na lista" cobre inexistente, inativo e sem role clínica de uma vez.
        when(client.listarProfissionais()).thenReturn(List.of(new ProfissionalDto(3L, "Enf. Ciclana", Set.of("ENFERMEIRO"))));

        assertThatThrownBy(() -> service.validar(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoIamIndisponivel() {
        assertThatThrownBy(() -> service.fallbackValidar(2L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
