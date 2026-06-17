package com.soulmv.iam.enums;

public enum StatusExame {
    SOLICITADO,
    COLETADO,
    EM_ANALISE,
    LIBERADO,
    CANCELADO;

    public boolean isFinal() {
        return this == LIBERADO || this == CANCELADO;
    }
}
