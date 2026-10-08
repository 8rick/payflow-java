package br.com.payflow.dtos;

import java.math.BigDecimal;

public record DepositoDTO(
        Long idUsuario,
        BigDecimal valor    
) {
}
