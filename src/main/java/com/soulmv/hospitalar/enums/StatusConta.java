package com.soulmv.hospitalar.enums;

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
