package br.com.payflow.controllers;

import br.com.payflow.dtos.DepositoDTO;
import br.com.payflow.dtos.TransactionDTO;
import br.com.payflow.models.Transaction;
import br.com.payflow.models.User;
import br.com.payflow.services.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {this.transacaoService = transacaoService;}


   @PostMapping("/deposito")
   public ResponseEntity<User> depoistar(@RequestBody @Valid DepositoDTO dto) {

        User usuario = transacaoService.depositar(dto.idUsuario(), dto.valor());

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
   }

   @PostMapping("/transferencia")
    public ResponseEntity<Transaction> transferir(@RequestBody @Valid TransactionDTO dto) {

       Transaction transaction = transacaoService.tranferir(
               dto.idRemetente(),
               dto.idDestinatario(),
               dto.valor()
       );

       return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
   }

}