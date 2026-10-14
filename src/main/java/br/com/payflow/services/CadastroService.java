package br.com.payflow.services;

import br.com.payflow.dtos.UserDTO;
import br.com.payflow.exceptions.RecursoNaoEncontradoException;
import br.com.payflow.exceptions.RegraDeNegocioException;
import br.com.payflow.models.User;
import br.com.payflow.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CadastroService {

    private final UserRepository repository;

    public CadastroService(UserRepository repository) {this.repository = repository;}


     public User createUser(UserDTO dto) {

       if(repository.existsByCpf(dto.cpf())) {
           throw new RegraDeNegocioException("CPF já cadastrado");
       }

       if(repository.existsByEmail(dto.email())) {
           throw  new RegraDeNegocioException("E-mail já cadastrado");
       }

       User newUser = new User();

       newUser.setNome(dto.nome());
       newUser.setEmail(dto.email());
       newUser.setCpf(dto.cpf());
       newUser.setSaldo(BigDecimal.ZERO);

       return repository.save(newUser);

    }

    public List<User> listarUsers() {
       return repository.findAll();
    }

    public User buscarIdUser(Long id) {
     return repository.findById(id)
        .orElseThrow(() ->
                new RecursoNaoEncontradoException(
                        "Usuário não encontrado com ID: " + id
                ));
    }


}
