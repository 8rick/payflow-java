package br.com.payflow.services;

import br.com.payflow.models.User;
import br.com.payflow.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.List;

public class CadastroService {

    private final UserRepository repository;

   public CadastroService(UserRepository repository) {this.repository = repository;}


    public User createUser(UserDTO dto) {

       User newUser = new User();
       newUser.setSaldo(BigDecimal.ZERO);

       newUser.setNome(dto.nome());
       newUser.setCpf(dto.cpf());
       newUser.setEmail(dto.email());

       return repository.save(newUser);
    }

    public List<User> listarUsers() {
       return repository.findAll();
    }

    public User buscarIdUser(Long id) {
     return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Nenhum usuário encontrado"));
    }




}
