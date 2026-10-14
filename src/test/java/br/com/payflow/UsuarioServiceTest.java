package br.com.payflow;


import br.com.payflow.dtos.UserDTO;
import br.com.payflow.exceptions.RegraDeNegocioException;
import br.com.payflow.models.User;
import br.com.payflow.repositories.UserRepository;
import br.com.payflow.services.CadastroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Dizemos ao JUnit para habilitar o Mockito
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock  // "Finge" ser o banco de dados
    private UserRepository repository;

    @InjectMocks // Injeta o banco de dados de mentira dentro do Service real
    private CadastroService service;

    @Test
    void naoDevePermitirCadastroComCpfJaExistente() {
        // Arranje (preparação)
        UserDTO dto = new UserDTO("Carlos", "111.111.111-11", "carlos@email.com");

        // Ensinamos o nosso banco falso a responder "TRUE" quando perguntarem se esse CPF existe
        when(repository.existsByCpf(dto.cpf())).thenReturn(true);


        // Act & Assert (Ação e Verificação)
        // Verificamos se ao chamar o método salvar, a nossa exceção customizada é lançada
        assertThrows(RegraDeNegocioException.class, () -> {
            service.createUser(dto);
        });

        // Verificamos que o método save() NUNCA chegou a ser chamado no banco
        verify(repository, never()).save(any(User.class));
    }

}
