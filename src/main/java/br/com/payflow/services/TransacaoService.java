package br.com.payflow.services;

import br.com.payflow.exceptions.RecursoNaoEncontradoException;
import br.com.payflow.exceptions.RegraDeNegocioException;
import br.com.payflow.exceptions.SaldoInsuficienteException;
import br.com.payflow.models.Transaction;
import br.com.payflow.models.User;
import br.com.payflow.repositories.TransactionsRepository;
import br.com.payflow.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransacaoService {

    private final UserRepository userRepository;
    private final TransactionsRepository transactionsRepository;

    public TransacaoService(TransactionsRepository transactionsRepository, UserRepository userRepository) {
        this.transactionsRepository = transactionsRepository;
        this.userRepository = userRepository;
    }

    public User depositar(Long idUsusario, BigDecimal valor) {

        if(valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException(
                    "O valor do depósito dever ser maior que zero"
            );
        }

        User usuario = userRepository.findById(idUsusario)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("O usuário " + idUsusario + " não foi encontrado")
                );

        usuario.setSaldo(usuario.getSaldo().add(valor));

        return userRepository.save(usuario);
    }

    @Transactional
    public Transaction tranferir(
            Long idRemetente,
            Long idDestinatario,
            BigDecimal valor
    ) {
        if(valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw  new RegraDeNegocioException(
                    "O valor deve ser maior que zero"
            );
        }

        User remetente = userRepository.findById(idRemetente)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("O remetente " + idRemetente + " emetente não econtrado")
                );

        User destinatario = userRepository.findById(idDestinatario)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("O destinatário " + idDestinatario + " não encontrado")

            );

        if(remetente.getId().equals(destinatario.getId())) {
            throw new RegraDeNegocioException("Não é permitodo transfeir para sim mesmo");
        }

        if(remetente.getSaldo().compareTo(valor) <= 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente para realizar a tranferência");
        }

        remetente.setSaldo(
                remetente.getSaldo().subtract(valor)
        );

        destinatario.setSaldo(
                destinatario.getSaldo().add(valor)
        );

        userRepository.save(remetente);
        userRepository.save(destinatario);

        Transaction transaction = new Transaction();

        transaction.setRemetente(remetente);
        transaction.setDestinatario(destinatario);
        transaction.setValue(valor);
        transaction.setDateTime(LocalDateTime.now());

        return transactionsRepository.save(transaction);

    }



}
