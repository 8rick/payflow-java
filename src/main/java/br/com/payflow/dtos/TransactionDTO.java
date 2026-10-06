package br.com.payflow.dtos;

import java.math.BigDecimal;

public record TransactionDTO(
        String idUser,
        BigDecimal valor
) {
}
