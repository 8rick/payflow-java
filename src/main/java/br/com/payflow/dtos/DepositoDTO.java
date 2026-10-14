package br.com.payflow.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoDTO(

        @NotNull
        Long idUsuario,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal valor
) {
}
