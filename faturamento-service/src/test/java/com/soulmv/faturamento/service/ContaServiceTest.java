package com.soulmv.faturamento.service;

import com.soulmv.faturamento.client.ProcedimentoTussDto;
import com.soulmv.faturamento.dto.request.ContaRequest;
import com.soulmv.faturamento.dto.request.ItemContaRequest;
import com.soulmv.faturamento.dto.response.ContaEstatisticasResponse;
import com.soulmv.faturamento.dto.response.ContaResponse;
import com.soulmv.faturamento.dto.response.GuiaTissResponse;
import com.soulmv.faturamento.entity.Atendimento;
import com.soulmv.faturamento.entity.ContaHospitalar;
import com.soulmv.faturamento.entity.Convenio;
import com.soulmv.faturamento.entity.GuiaTiss;
import com.soulmv.faturamento.entity.ItemConta;
import com.soulmv.faturamento.entity.Paciente;
import com.soulmv.faturamento.enums.StatusConta;
import com.soulmv.faturamento.exception.BusinessException;
import com.soulmv.faturamento.exception.ResourceNotFoundException;
import com.soulmv.faturamento.mapper.FaturamentoMapper;
import com.soulmv.faturamento.repository.AtendimentoRepository;
import com.soulmv.faturamento.repository.ContaHospitalarRepository;
import com.soulmv.faturamento.repository.GuiaTissRepository;
import com.soulmv.faturamento.repository.ItemContaRepository;
import com.soulmv.faturamento.service.faturamento.TissXmlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock ContaHospitalarRepository repository;
    @Mock AtendimentoRepository atendimentoRepository;
    @Mock ProcedimentoTussLookupService procedimentoLookup;
    @Mock ItemContaRepository itemContaRepository;
    @Mock GuiaTissRepository guiaRepository;
    @Mock FaturamentoMapper mapper;
    @Mock TissXmlBuilder tissXmlBuilder;

    @InjectMocks
    ContaService service;

    private Paciente paciente;
    private Convenio convenio;
    private Atendimento atendimento;

    @BeforeEach
    void setUp() {
        convenio = Convenio.builder().nome("Unimed").build();
        convenio.setId(2L);

        paciente = Paciente.builder().nome("Fulano de Tal").convenio(convenio).build();
        paciente.setId(1L);

        atendimento = Atendimento.builder().paciente(paciente).build();
        atendimento.setId(10L);
    }

    private ContaHospitalar contaAberta(Long id) {
        ContaHospitalar conta = ContaHospitalar.builder()
                .atendimento(atendimento)
                .convenio(convenio)
                .status(StatusConta.ABERTA)
                .valorTotal(BigDecimal.ZERO)
                .itens(new ArrayList<>())
                .build();
        conta.setId(id);
        return conta;
    }

    private ProcedimentoTussDto procedimento(Long id, BigDecimal valorReferencia, boolean ativo) {
        return new ProcedimentoTussDto(id, "100", "Consulta", valorReferencia, ativo);
    }

    // ---------------------------------------------------------------- abrir

    @Test
    void abrir_deveCriarConta_quandoAtendimentoExisteESemContaPrevia() {
        ContaRequest request = new ContaRequest(10L);
        when(atendimentoRepository.findById(10L)).thenReturn(Optional.of(atendimento));
        when(repository.existsByAtendimentoId(10L)).thenReturn(false);
        when(repository.save(any(ContaHospitalar.class))).thenAnswer(inv -> inv.getArgument(0));
        ContaResponse esperado = new ContaResponse(null, 10L, 1L, "Fulano de Tal", 2L, "Unimed",
                StatusConta.ABERTA, BigDecimal.ZERO, null, List.of(), null);
        when(mapper.toResponse(any(ContaHospitalar.class))).thenReturn(esperado);

        ContaResponse resposta = service.abrir(request);

        ArgumentCaptor<ContaHospitalar> captor = ArgumentCaptor.forClass(ContaHospitalar.class);
        verify(repository).save(captor.capture());
        ContaHospitalar salva = captor.getValue();
        assertThat(salva.getStatus()).isEqualTo(StatusConta.ABERTA);
        assertThat(salva.getAtendimento()).isEqualTo(atendimento);
        assertThat(salva.getConvenio()).isEqualTo(convenio);
        assertThat(salva.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resposta).isEqualTo(esperado);
    }

    @Test
    void abrir_deveFalhar_quandoAtendimentoNaoExiste() {
        ContaRequest request = new ContaRequest(999L);
        when(atendimentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.abrir(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Atendimento");

        verify(repository, never()).save(any());
    }

    @Test
    void abrir_deveFalhar_quandoContaJaExisteParaAtendimento() {
        ContaRequest request = new ContaRequest(10L);
        when(atendimentoRepository.findById(10L)).thenReturn(Optional.of(atendimento));
        when(repository.existsByAtendimentoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.abrir(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui conta")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(repository, never()).save(any());
    }

    // --------------------------------------------------------- adicionarItem

    @Test
    void adicionarItem_deveRecalcularTotal_quandoSucesso() {
        ContaHospitalar conta = contaAberta(5L);
        ProcedimentoTussDto proc = procedimento(7L, BigDecimal.TEN, true);
        ItemContaRequest request = new ItemContaRequest(7L, 2, null);

        when(repository.findById(5L)).thenReturn(Optional.of(conta));
        when(procedimentoLookup.buscar(7L)).thenReturn(proc);
        when(itemContaRepository.save(any(ItemConta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(ContaHospitalar.class))).thenAnswer(inv -> inv.getArgument(0));
        ContaResponse esperado = new ContaResponse(5L, 10L, 1L, "Fulano de Tal", 2L, "Unimed",
                StatusConta.ABERTA, BigDecimal.valueOf(20), null, List.of(), null);
        when(mapper.toResponse(any(ContaHospitalar.class))).thenReturn(esperado);

        ContaResponse resposta = service.adicionarItem(5L, request);

        ArgumentCaptor<ItemConta> itemCaptor = ArgumentCaptor.forClass(ItemConta.class);
        verify(itemContaRepository).save(itemCaptor.capture());
        ItemConta itemSalvo = itemCaptor.getValue();
        assertThat(itemSalvo.getValorUnitario()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(itemSalvo.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(20));

        assertThat(conta.getItens()).hasSize(1);
        assertThat(conta.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(resposta).isEqualTo(esperado);
    }

    @Test
    void adicionarItem_deveFalhar_quandoContaNaoAberta() {
        ContaHospitalar conta = contaAberta(5L);
        conta.setStatus(StatusConta.FECHADA);
        ItemContaRequest request = new ItemContaRequest(7L, 1, BigDecimal.TEN);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.adicionarItem(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está aberta");

        verify(procedimentoLookup, never()).buscar(any());
        verify(itemContaRepository, never()).save(any());
    }

    @Test
    void adicionarItem_deveFalhar_quandoProcedimentoInexistente() {
        ContaHospitalar conta = contaAberta(5L);
        ItemContaRequest request = new ItemContaRequest(999L, 1, BigDecimal.TEN);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));
        when(procedimentoLookup.buscar(999L)).thenThrow(new ResourceNotFoundException("Procedimento TUSS", 999L));

        assertThatThrownBy(() -> service.adicionarItem(5L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Procedimento TUSS");

        verify(itemContaRepository, never()).save(any());
    }

    @Test
    void adicionarItem_deveFalhar_quandoProcedimentoInativo() {
        ContaHospitalar conta = contaAberta(5L);
        ProcedimentoTussDto proc = procedimento(7L, BigDecimal.TEN, false);
        ItemContaRequest request = new ItemContaRequest(7L, 1, null);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));
        when(procedimentoLookup.buscar(7L)).thenReturn(proc);

        assertThatThrownBy(() -> service.adicionarItem(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Procedimento inativo");

        verify(itemContaRepository, never()).save(any());
    }

    @Test
    void adicionarItem_deveFalhar_quandoFaltaValorUnitarioESemValorReferencia() {
        ContaHospitalar conta = contaAberta(5L);
        ProcedimentoTussDto proc = procedimento(7L, null, true);
        ItemContaRequest request = new ItemContaRequest(7L, 1, null);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));
        when(procedimentoLookup.buscar(7L)).thenReturn(proc);

        assertThatThrownBy(() -> service.adicionarItem(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Informe o valor unitário");

        verify(itemContaRepository, never()).save(any());
    }

    // -------------------------------------------------------------- fechar

    @Test
    void fechar_devefecharConta_quandoAbertaComItens() {
        ContaHospitalar conta = contaAberta(5L);
        ItemConta item = ItemConta.builder().conta(conta)
                .procedimentoId(7L).codigoTuss("100").descricao("Consulta")
                .quantidade(1).valorUnitario(BigDecimal.TEN).valorTotal(BigDecimal.TEN).build();
        conta.getItens().add(item);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));
        when(repository.save(any(ContaHospitalar.class))).thenAnswer(inv -> inv.getArgument(0));
        ContaResponse esperado = new ContaResponse(5L, 10L, 1L, "Fulano de Tal", 2L, "Unimed",
                StatusConta.FECHADA, BigDecimal.TEN, null, List.of(), null);
        when(mapper.toResponse(any(ContaHospitalar.class))).thenReturn(esperado);

        ContaResponse resposta = service.fechar(5L);

        ArgumentCaptor<ContaHospitalar> captor = ArgumentCaptor.forClass(ContaHospitalar.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusConta.FECHADA);
        assertThat(captor.getValue().getDataFechamento()).isNotNull();
        assertThat(resposta).isEqualTo(esperado);
    }

    @Test
    void fechar_deveFalhar_quandoContaNaoAberta() {
        ContaHospitalar conta = contaAberta(5L);
        conta.setStatus(StatusConta.FATURADA);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.fechar(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Apenas contas abertas podem ser fechadas");

        verify(repository, never()).save(any());
    }

    @Test
    void fechar_deveFalhar_quandoSemItens() {
        ContaHospitalar conta = contaAberta(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.fechar(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sem itens");

        verify(repository, never()).save(any());
    }

    // -------------------------------------------------------- gerarGuiaTiss

    @Test
    void gerarGuiaTiss_deveGerarGuia_quandoContaFechada() {
        ContaHospitalar conta = contaAberta(5L);
        conta.setStatus(StatusConta.FECHADA);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));
        when(guiaRepository.count()).thenReturn(3L);
        when(tissXmlBuilder.gerar(any(ContaHospitalar.class), anyString(), any())).thenReturn("<xml/>");
        when(guiaRepository.save(any(GuiaTiss.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(ContaHospitalar.class))).thenAnswer(inv -> inv.getArgument(0));
        GuiaTissResponse esperado = new GuiaTissResponse(null, 5L, "0000000004", null, null);
        when(mapper.toResponse(any(GuiaTiss.class))).thenReturn(esperado);

        GuiaTissResponse resposta = service.gerarGuiaTiss(5L);

        ArgumentCaptor<GuiaTiss> guiaCaptor = ArgumentCaptor.forClass(GuiaTiss.class);
        verify(guiaRepository).save(guiaCaptor.capture());
        assertThat(guiaCaptor.getValue().getNumeroGuia()).isEqualTo("0000000004");
        assertThat(guiaCaptor.getValue().getXml()).isEqualTo("<xml/>");

        ArgumentCaptor<ContaHospitalar> contaCaptor = ArgumentCaptor.forClass(ContaHospitalar.class);
        verify(repository).save(contaCaptor.capture());
        assertThat(contaCaptor.getValue().getStatus()).isEqualTo(StatusConta.FATURADA);

        assertThat(resposta).isEqualTo(esperado);
    }

    @Test
    void gerarGuiaTiss_deveFalhar_quandoContaNaoFechada() {
        ContaHospitalar conta = contaAberta(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.gerarGuiaTiss(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("precisa estar FECHADA");

        verify(guiaRepository, never()).save(any());
        verify(tissXmlBuilder, never()).gerar(any(), anyString(), any());
    }

    // ------------------------------------------------------------ estatisticas

    @Test
    void estatisticas_deveSomarTotaisEValoresPorStatus() {
        when(repository.count()).thenReturn(10L);
        when(repository.somaValorTotal()).thenReturn(new BigDecimal("15000.00"));
        when(repository.countByStatus(StatusConta.ABERTA)).thenReturn(4L);
        when(repository.countByStatus(StatusConta.FECHADA)).thenReturn(2L);
        when(repository.countByStatus(StatusConta.FATURADA)).thenReturn(3L);
        when(repository.countByStatus(StatusConta.GLOSADA)).thenReturn(1L);
        when(repository.countByStatus(StatusConta.CANCELADA)).thenReturn(0L);
        when(repository.somaValorTotalPorStatus(any(StatusConta.class))).thenReturn(BigDecimal.ZERO);
        when(repository.somaValorTotalPorStatus(StatusConta.FATURADA)).thenReturn(new BigDecimal("9000.00"));

        ContaEstatisticasResponse resultado = service.estatisticas();

        assertThat(resultado.total()).isEqualTo(10L);
        assertThat(resultado.valorTotal()).isEqualByComparingTo("15000.00");
        assertThat(resultado.contasPorStatus())
                .containsEntry("ABERTA", 4L)
                .containsEntry("FECHADA", 2L)
                .containsEntry("FATURADA", 3L)
                .containsEntry("GLOSADA", 1L)
                .containsEntry("CANCELADA", 0L);
        assertThat(resultado.valorPorStatus().get("FATURADA")).isEqualByComparingTo("9000.00");
    }
}
