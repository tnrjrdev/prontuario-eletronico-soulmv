package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.AdministracaoResponse;
import com.soulmv.hospitalar.dto.response.AnamneseResponse;
import com.soulmv.hospitalar.dto.response.DiagnosticoResponse;
import com.soulmv.hospitalar.dto.response.ItemPrescricaoResponse;
import com.soulmv.hospitalar.dto.response.PrescricaoResponse;
import com.soulmv.hospitalar.dto.response.ResultadoExameResponse;
import com.soulmv.hospitalar.dto.response.SolicitacaoExameResponse;
import com.soulmv.hospitalar.entity.AdministracaoMedicamento;
import com.soulmv.hospitalar.entity.Anamnese;
import com.soulmv.hospitalar.entity.Diagnostico;
import com.soulmv.hospitalar.entity.ItemPrescricao;
import com.soulmv.hospitalar.entity.Prescricao;
import com.soulmv.hospitalar.entity.ResultadoExame;
import com.soulmv.hospitalar.entity.SolicitacaoExame;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversões entidade → DTO de saída do módulo clínico (Etapa 7).
 */
@Mapper(componentModel = "spring")
public interface ClinicoMapper {

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "medicoId", source = "medico.id")
    @Mapping(target = "medicoNome", source = "medico.nomeCompleto")
    AnamneseResponse toResponse(Anamnese anamnese);

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "cid10Id", source = "cid10.id")
    @Mapping(target = "cid10Codigo", source = "cid10.codigo")
    @Mapping(target = "cid10Descricao", source = "cid10.descricao")
    @Mapping(target = "medicoId", source = "medico.id")
    @Mapping(target = "medicoNome", source = "medico.nomeCompleto")
    DiagnosticoResponse toResponse(Diagnostico diagnostico);

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "medicoId", source = "medico.id")
    @Mapping(target = "medicoNome", source = "medico.nomeCompleto")
    PrescricaoResponse toResponse(Prescricao prescricao);

    @Mapping(target = "medicamentoId", source = "medicamento.id")
    @Mapping(target = "medicamentoNome", source = "medicamento.nome")
    @Mapping(target = "medicamentoControlado", source = "medicamento.controlado")
    ItemPrescricaoResponse toResponse(ItemPrescricao item);

    @Mapping(target = "itemPrescricaoId", source = "itemPrescricao.id")
    @Mapping(target = "medicamentoNome", source = "itemPrescricao.medicamento.nome")
    @Mapping(target = "enfermeiroId", source = "enfermeiro.id")
    @Mapping(target = "enfermeiroNome", source = "enfermeiro.nomeCompleto")
    AdministracaoResponse toResponse(AdministracaoMedicamento administracao);

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "medicoSolicitanteId", source = "medicoSolicitante.id")
    @Mapping(target = "medicoSolicitanteNome", source = "medicoSolicitante.nomeCompleto")
    SolicitacaoExameResponse toResponse(SolicitacaoExame solicitacao);

    @Mapping(target = "laudoAnexoId", source = "laudo.id")
    @Mapping(target = "temLaudo", expression = "java(resultado.getLaudo() != null)")
    @Mapping(target = "liberadoPorId", source = "liberadoPor.id")
    @Mapping(target = "liberadoPorNome", source = "liberadoPor.nomeCompleto")
    ResultadoExameResponse toResponse(ResultadoExame resultado);
}
