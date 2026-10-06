package br.com.payflow.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDTO (

        @NotBlank(message = "O nome é obrigatório e não pode ser apenas epaços em branco")
        @Size(min = 3, max = 120)
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        int cpf,

        @NotBlank(message = "O email é obrigatório")
        String email
) {

}
