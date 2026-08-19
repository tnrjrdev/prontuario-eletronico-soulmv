package com.soulmv.exames.dto.request;

import com.soulmv.exames.enums.StatusExame;
import jakarta.validation.constraints.NotNull;

public record ExameStatusRequest(@NotNull StatusExame status) {
}
