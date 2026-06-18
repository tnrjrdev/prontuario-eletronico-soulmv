package com.soulmv.faturamento.mapper;

import com.soulmv.faturamento.dto.response.ContaResponse;
import com.soulmv.faturamento.dto.response.GuiaTissResponse;
import com.soulmv.faturamento.dto.response.ItemContaResponse;
import com.soulmv.faturamento.entity.ContaHospitalar;
import com.soulmv.faturamento.entity.GuiaTiss;
import com.soulmv.faturamento.entity.ItemConta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FaturamentoMapper {

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "pacienteId", source = "atendimento.paciente.id")
    @Mapping(target = "pacienteNome", source = "atendimento.paciente.nome")
    @Mapping(target = "convenioId", source = "convenio.id")
    @Mapping(target = "convenioNome", source = "convenio.nome")
    ContaResponse toResponse(ContaHospitalar conta);

    @Mapping(target = "procedimentoId", source = "procedimento.id")
    @Mapping(target = "codigoTuss", source = "procedimento.codigoTuss")
    @Mapping(target = "descricao", source = "procedimento.descricao")
    ItemContaResponse toResponse(ItemConta item);

    @Mapping(target = "contaId", source = "conta.id")
    GuiaTissResponse toResponse(GuiaTiss guia);
}
