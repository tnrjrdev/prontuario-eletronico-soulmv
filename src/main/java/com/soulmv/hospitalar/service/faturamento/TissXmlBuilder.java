package com.soulmv.hospitalar.service.faturamento;

import com.soulmv.hospitalar.entity.ContaHospitalar;
import com.soulmv.hospitalar.entity.ItemConta;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Gera o XML da guia TISS (estrutura SP/SADT simplificada, padrão ANS).
 *
 * NOTA: este é um subconjunto representativo e bem-formado do TISS, suficiente
 * para integração interna e pronto para ser estendido ao XSD oficial da ANS
 * (ans:mensagemTISS completo, com cabeçalho, hash e epílogo).
 */
@Component
public class TissXmlBuilder {

    private static final String NS = "http://www.ans.gov.br/padroes/tiss/schemas";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String gerar(ContaHospitalar conta, String numeroGuia, LocalDate dataGeracao) {
        var paciente = conta.getAtendimento().getPaciente();
        String convenio = conta.getConvenio() != null ? conta.getConvenio().getNome() : "PARTICULAR";
        String registroAns = conta.getConvenio() != null && conta.getConvenio().getRegistroAns() != null
                ? conta.getConvenio().getRegistroAns() : "000000";

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<ans:mensagemTISS xmlns:ans=\"").append(NS).append("\">\n");
        sb.append("  <ans:cabecalho>\n");
        sb.append("    <ans:identificacaoTransacao>\n");
        sb.append("      <ans:tipoTransacao>ENVIO_LOTE_GUIAS</ans:tipoTransacao>\n");
        sb.append("      <ans:numeroLote>").append(esc(numeroGuia)).append("</ans:numeroLote>\n");
        sb.append("      <ans:dataRegistroTransacao>").append(dataGeracao.format(DATA))
                .append("</ans:dataRegistroTransacao>\n");
        sb.append("    </ans:identificacaoTransacao>\n");
        sb.append("    <ans:origem><ans:registroANS>").append(esc(registroAns))
                .append("</ans:registroANS></ans:origem>\n");
        sb.append("  </ans:cabecalho>\n");

        sb.append("  <ans:guiaSP-SADT>\n");
        sb.append("    <ans:numeroGuiaPrestador>").append(esc(numeroGuia))
                .append("</ans:numeroGuiaPrestador>\n");
        sb.append("    <ans:dadosBeneficiario>\n");
        sb.append("      <ans:nomeBeneficiario>").append(esc(paciente.getNome()))
                .append("</ans:nomeBeneficiario>\n");
        sb.append("      <ans:numeroCarteira>")
                .append(esc(paciente.getNumeroCarteirinha() != null ? paciente.getNumeroCarteirinha() : ""))
                .append("</ans:numeroCarteira>\n");
        sb.append("      <ans:nomePlano>").append(esc(convenio)).append("</ans:nomePlano>\n");
        sb.append("    </ans:dadosBeneficiario>\n");

        sb.append("    <ans:procedimentosExecutados>\n");
        for (ItemConta item : conta.getItens()) {
            sb.append("      <ans:procedimentoExecutado>\n");
            sb.append("        <ans:codigoProcedimento>").append(esc(item.getProcedimento().getCodigoTuss()))
                    .append("</ans:codigoProcedimento>\n");
            sb.append("        <ans:descricaoProcedimento>").append(esc(item.getProcedimento().getDescricao()))
                    .append("</ans:descricaoProcedimento>\n");
            sb.append("        <ans:quantidadeExecutada>").append(item.getQuantidade())
                    .append("</ans:quantidadeExecutada>\n");
            sb.append("        <ans:valorUnitario>").append(item.getValorUnitario().toPlainString())
                    .append("</ans:valorUnitario>\n");
            sb.append("        <ans:valorTotal>").append(item.getValorTotal().toPlainString())
                    .append("</ans:valorTotal>\n");
            sb.append("      </ans:procedimentoExecutado>\n");
        }
        sb.append("    </ans:procedimentosExecutados>\n");
        sb.append("    <ans:valorTotalGeral>").append(conta.getValorTotal().toPlainString())
                .append("</ans:valorTotalGeral>\n");
        sb.append("  </ans:guiaSP-SADT>\n");
        sb.append("</ans:mensagemTISS>\n");
        return sb.toString();
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
