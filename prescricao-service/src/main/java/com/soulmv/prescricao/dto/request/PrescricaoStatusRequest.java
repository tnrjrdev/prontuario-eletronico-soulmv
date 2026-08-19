package com.soulmv.prescricao.dto.request;

import com.soulmv.prescricao.enums.StatusPrescricao;
import jakarta.validation.constraints.NotNull;

public record PrescricaoStatusRequest(@NotNull StatusPrescricao status) {
}
