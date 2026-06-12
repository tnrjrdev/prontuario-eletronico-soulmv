package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AlocarLeitoRequest;
import com.soulmv.hospitalar.dto.request.AtendimentoStatusRequest;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Leito;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.StatusLeito;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.mapper.AtendimentoMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.LeitoRepository;
import com.soulmv.hospitalar.repository.PacienteRepository;
import com.soulmv.hospitalar.repository.SetorRepository;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock AtendimentoRepository atendimentoRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock SetorRepository setorRepository;
    @Mock LeitoRepository leitoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AtendimentoMapper mapper;

    @InjectMocks AtendimentoService service;

    @Test
    void atualizarStatus_deveFalhar_quandoAtendimentoEncerrado() {
        Atendimento at = Atendimento.builder().status(StatusAtendimento.ALTA).build();
        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(at));

        assertThatThrownBy(() -> service.atualizarStatus(1L,
                new AtendimentoStatusRequest(StatusAtendimento.EM_ATENDIMENTO), "user"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");
    }

    @Test
    void alocarLeito_deveFalhar_quandoLeitoOcupado() {
        Atendimento at = Atendimento.builder().status(StatusAtendimento.EM_ATENDIMENTO).build();
        Leito leito = Leito.builder().status(StatusLeito.OCUPADO).ativo(true).build();
        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(at));
        when(leitoRepository.findById(5L)).thenReturn(Optional.of(leito));

        assertThatThrownBy(() -> service.alocarLeito(1L, new AlocarLeitoRequest(5L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void darAlta_deveEncerrarAtendimento_eLiberarLeito() {
        Leito leito = Leito.builder().status(StatusLeito.OCUPADO).ativo(true).build();
        Atendimento at = Atendimento.builder()
                .status(StatusAtendimento.INTERNADO)
                .leito(leito)
                .profissionalResponsavel(new Usuario())
                .build();
        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(at));
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.darAlta(1L, "medico");

        assertThat(at.getStatus()).isEqualTo(StatusAtendimento.ALTA);
        assertThat(at.getDataAlta()).isNotNull();
        assertThat(at.getLeito()).isNull();
        assertThat(leito.getStatus()).isEqualTo(StatusLeito.HIGIENIZACAO);
    }
}
