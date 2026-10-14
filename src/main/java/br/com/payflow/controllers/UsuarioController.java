package br.com.payflow.controllers;

import br.com.payflow.dtos.UserDTO;
import br.com.payflow.models.User;
import br.com.payflow.services.CadastroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final CadastroService cadastroservice;

    public UsuarioController(CadastroService service) {this.cadastroservice = service;}


    @PostMapping
    public ResponseEntity<User> criarUser(@RequestBody @Valid UserDTO dto) {

        User usuario = cadastroservice.createUser(dto);

     return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> listarUsers() {
        return ResponseEntity.ok(cadastroservice.listarUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> buscarIdUser(@PathVariable Long id) {
        User usuario = cadastroservice.buscarIdUser(id);

        return ResponseEntity.ok(usuario);
    }

}
