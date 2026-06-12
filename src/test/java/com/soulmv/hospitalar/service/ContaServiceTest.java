package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.ItemContaRequest;
import com.soulmv.hospitalar.entity.ContaHospitalar;
import com.soulmv.hospitalar.enums.StatusConta;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.mapper.FaturamentoMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.ContaHospitalarRepository;
import com.soulmv.hospitalar.repository.GuiaTissRepository;
import com.soulmv.hospitalar.repository.ItemContaRepository;
import com.soulmv.hospitalar.repository.ProcedimentoTussRepository;
import com.soulmv.hospitalar.service.faturamento.TissXmlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock ContaHospitalarRepository repository;
    @Mock AtendimentoRepository atendimentoRepository;
    @Mock ProcedimentoTussRepository procedimentoRepository;
    @Mock ItemContaRepository itemContaRepository;
    @Mock GuiaTissRepository guiaRepository;
    @Mock FaturamentoMapper mapper;
    @Mock TissXmlBuilder tissXmlBuilder;

    @InjectMocks ContaService service;

    @Test
    void adicionarItem_deveFalhar_quandoContaNaoAberta() {
        ContaHospitalar conta = ContaHospitalar.builder().status(StatusConta.FECHADA).build();
        when(repository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.adicionarItem(1L,
                new ItemContaRequest(10L, 1, BigDecimal.TEN)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aberta");
    }

    @Test
    void fechar_deveFalhar_quandoSemItens() {
        ContaHospitalar conta = ContaHospitalar.builder().status(StatusConta.ABERTA).build();
        when(repository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.fechar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sem itens");
    }

    @Test
    void gerarGuiaTiss_deveFalhar_quandoContaNaoFechada() {
        ContaHospitalar conta = ContaHospitalar.builder().status(StatusConta.ABERTA).build();
        when(repository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.gerarGuiaTiss(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FECHADA");
    }
}
