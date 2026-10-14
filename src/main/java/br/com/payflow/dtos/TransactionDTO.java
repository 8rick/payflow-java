package br.com.payflow.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionDTO(

        @NotNull
        Long idRemetente,

        @NotNull
        Long idDestinatario,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal valor
) {
}
