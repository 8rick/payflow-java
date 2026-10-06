package br.com.payflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String nome;

    @Column(nullable = false, unique = true)
    String cpf;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    BigDecimal saldo;
}
