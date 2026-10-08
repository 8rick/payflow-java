package br.com.payflow.dtos;

import java.math.BigDecimal;

public record TransactionDTO(
        Long idRemetente,
        Long idDestino,
        BigDecimal valor
) {
}
