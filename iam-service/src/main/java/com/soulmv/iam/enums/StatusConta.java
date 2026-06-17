package com.soulmv.iam.enums;

public enum StatusConta {
    ABERTA,
    FECHADA,
    FATURADA,
    GLOSADA,
    CANCELADA;

    public boolean permiteEdicaoItens() {
        return this == ABERTA;
    }
}
