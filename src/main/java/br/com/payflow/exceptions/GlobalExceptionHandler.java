package br.com.payflow.exceptions;

import br.com.payflow.dtos.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiErrorDTO> handleRegraDeNegocio(
            RegraDeNegocioException ex,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorDTO> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ApiErrorDTO> handleSaldoInsuficiente(
            SaldoInsuficienteException ex,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> detalhes = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        erro -> erro.getDefaultMessage() != null
                                ? erro.getDefaultMessage()
                                : "Valor inválido",
                        (primeiraMensagem, segundaMensagem) -> primeiraMensagem,
                        LinkedHashMap::new
                ));

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos são inválidos.",
                request,
                detalhes
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleCorpoInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou contém JSON inválido.",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleErroInesperado(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Erro inesperado ao processar a requisição: {}",
                request.getRequestURI(),
                ex
        );

        return criarResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Tente novamente mais tarde.",
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorDTO> criarResposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> detalhes
    ) {
        ApiErrorDTO resposta = new ApiErrorDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                detalhes
        );

        return ResponseEntity.status(status).body(resposta);
    }
}