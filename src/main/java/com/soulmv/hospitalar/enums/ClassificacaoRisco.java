package com.soulmv.hospitalar.enums;

/**
 * Classificação de risco (protocolo de Manchester), da maior para a menor
 * gravidade. {@code prioridade} 0 = mais grave (atendimento imediato).
 */
public enum ClassificacaoRisco {
    VERMELHO("Emergência", 0, 0),
    LARANJA("Muito urgente", 1, 10),
    AMARELO("Urgente", 2, 60),
    VERDE("Pouco urgente", 3, 120),
    AZUL("Não urgente", 4, 240);

    private final String descricao;
    private final int prioridade;
    private final int tempoMaximoMinutos;

    ClassificacaoRisco(String descricao, int prioridade, int tempoMaximoMinutos) {
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.tempoMaximoMinutos = tempoMaximoMinutos;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public int getTempoMaximoMinutos() {
        return tempoMaximoMinutos;
    }
}
