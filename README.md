# PayFlow API

API REST para uma carteira digital, desenvolvida com Java e Spring Boot. O projeto tem foco em cadastro de usuários, depósitos e transferências entre contas, com persistência em PostgreSQL, validação de dados, tratamento centralizado de erros, testes automatizados e execução em Docker.

> **Status:** projeto em desenvolvimento. A API já foi executada localmente e em contêiner Docker, conectando-se ao PostgreSQL hospedado no Neon. A esteira do GitHub Actions foi configurada para executar a verificação Maven; a publicação automática da imagem no GitHub Container Registry está em fase de validação. Ainda não há deploy público da API confirmado.

## Índice

- [Objetivos](#objetivos)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Funcionalidades e regras de negócio](#funcionalidades-e-regras-de-negócio)
- [Endpoints](#endpoints)
- [Modelagem de dados](#modelagem-de-dados)
- [Tratamento de erros](#tratamento-de-erros)
- [Configuração local](#configuração-local)
- [Docker](#docker)
- [Testes com Postman](#testes-com-postman)
- [CI/CD com GitHub Actions](#cicd-com-github-actions)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Limitações e próximos passos](#limitações-e-próximos-passos)
- [Segurança](#segurança)
- [Licença](#licença)

## Objetivos

- Praticar desenvolvimento de APIs REST com Spring Boot.
- Implementar regras básicas de uma carteira digital.
- Persistir usuários e transações em PostgreSQL.
- Validar entradas e retornar erros HTTP consistentes.
- Aprender testes automatizados, Docker e integração contínua.
- Preparar o projeto para uma futura implantação em hospedagem.

## Tecnologias

- **Java 25**
- **Spring Boot 4.1.1**
- Spring Web MVC — endpoints HTTP REST.
- Spring Data JPA / Hibernate — persistência e mapeamento objeto-relacional.
- Jakarta Validation — validação de DTOs.
- PostgreSQL — banco de dados relacional.
- Neon — PostgreSQL gerenciado utilizado no ambiente de desenvolvimento.
- Maven Wrapper — build reproduzível sem exigir instalação global do Maven.
- Lombok — redução de código repetitivo em classes Java.
- Postman — execução de requisições e assertions de API.
- Newman — execução de coleções Postman pela linha de comando (configuração planejada/uso a confirmar).
- Docker — empacotamento e execução da API em contêiner.
- GitHub Actions — automação de build e testes.
- GitHub Container Registry (GHCR) — destino planejado para publicar a imagem Docker.

## Arquitetura

O projeto está organizado por responsabilidades:

- **controllers**: recebem requisições HTTP, chamam os serviços e retornam respostas.
- **dtos**: definem os dados aceitos pelas requisições e/ou retornados pela API.
- **models**: entidades persistidas no banco.
- **repositories**: acesso aos dados por meio do Spring Data JPA.
- **services**: regras de negócio e coordenação das operações.
- **exceptions**: exceções de negócio e tratamento global de erros.

Fluxo típico de uma requisição:

`Cliente HTTP → Controller → Service → Repository → PostgreSQL`

As validações de entrada são aplicadas nos DTOs com Jakarta Validation e `@Valid`. As exceções da aplicação são convertidas em respostas HTTP por um `@RestControllerAdvice`.

## Funcionalidades e regras de negócio

### Cadastro e consulta de usuário

- Criar usuário com nome, CPF e e-mail.
- CPF e e-mail devem ser únicos.
- O saldo inicial de um usuário novo deve ser zero.
- Consultar usuário pelo ID.
- Quando o ID não existir, a API deve retornar erro de recurso não encontrado.

### Depósito

- Recebe o ID do usuário e o valor do depósito.
- O valor deve ser maior que zero.
- O depósito deve aumentar o saldo do usuário.
- Usuário inexistente deve resultar em erro.
- O endpoint responde com HTTP 200 quando a operação é aceita.

### Transferência

- Recebe o ID do remetente, o ID do destinatário e o valor.
- O valor deve ser maior que zero.
- O remetente precisa ter saldo suficiente.
- Não deve ser permitida transferência para a própria conta.
- O saldo do remetente é debitado e o do destinatário é creditado pelo mesmo valor.
- A transferência deve registrar uma transação com remetente, destinatário, valor e data/hora.
- A operação deve ser atômica: atualização dos saldos e gravação da transação devem ocorrer na mesma transação de banco de dados. O serviço de transferência deve utilizar `@Transactional`.

**Nota:** essas são as regras esperadas a partir da implementação planejada. Antes de considerar o sistema pronto para produção, elas devem ser confirmadas por testes automatizados de unidade e integração.

## Endpoints

Base local: `http://localhost:8080`

| Método | Caminho | Finalidade | Sucesso esperado |
|---|---|---|---|
| `POST` | `/usuarios` | Cadastrar usuário | `201 Created` |
| `GET` | `/usuarios/{id}` | Consultar usuário por ID | `200 OK` |
| `POST` | `/transacoes/deposito` | Depositar saldo | `200 OK` |
| `POST` | `/transacoes/transferencia` | Transferir saldo entre usuários | `200 OK` |

### 1. Cadastrar usuário

`POST /usuarios`

Exemplo ilustrativo de corpo (ajuste os campos conforme o `UserDTO` atual):

```json
{
  "nome": "Ana Silva",
  "cpf": "52998224725",
  "email": "ana@example.com"
}
```

O saldo inicial deve ser definido pelo serviço como zero, não enviado pelo cliente.

### 2. Consultar usuário

`GET /usuarios/{id}`

Exemplo: `GET http://localhost:8080/usuarios/1`

Retorna os dados do usuário encontrado ou um erro 404 se o ID não existir.

### 3. Depositar

`POST /transacoes/deposito`

```json
{
  "idUsuario": 1,
  "valor": 100.00
}
```

O DTO de depósito utiliza `idUsuario` e `valor`; o valor deve ser positivo.

### 4. Transferir

`POST /transacoes/transferencia`

```json
{
  "idRemetente": 1,
  "idDestinatario": 2,
  "valor": 30.00
}
```

Exemplo de cenário de teste: após um depósito de R$ 100, uma transferência de R$ 30 deve deixar o remetente com R$ 70 e o destinatário com R$ 30, supondo que ambos começaram com saldo zero e não existam outras operações concorrentes.

## Modelagem de dados

### Usuário (`User`)

Campos conhecidos da entidade:

| Campo | Tipo conceitual | Regra |
|---|---|---|
| `id` | Long | Chave primária gerada pelo banco |
| `nome` | String | Obrigatório, até 100 caracteres |
| `cpf` | String | Obrigatório e único |
| `email` | String | Obrigatório e único |
| `saldo` | BigDecimal | Inicializado em zero |

Tabela: `tb_user`.

### Transação (`Transaction`)

Campos conhecidos da entidade:

| Campo | Tipo conceitual | Descrição |
|---|---|---|
| `id` | Long | Chave primária |
| `remetente` | User | Usuário debitado |
| `destinatario` | User | Usuário creditado |
| `valor` | BigDecimal | Quantia transferida |
| `dataHora` | LocalDateTime | Data e hora do registro |

Tabela: `tb_transaction`. Os nomes finais dos campos e colunas devem ser conferidos no código atual.

Valores monetários usam `BigDecimal`, evitando erros de precisão típicos de `float` e `double`.

## Tratamento de erros

A API utiliza um handler global para converter exceções em respostas JSON. Os status previstos são:

| Situação | HTTP |
|---|---:|
| Regra de negócio inválida, CPF/e-mail duplicado ou valor inválido | `400 Bad Request` |
| Usuário/recurso não encontrado | `404 Not Found` |
| Saldo insuficiente | `422 Unprocessable Entity` |
| Erro inesperado não tratado | `500 Internal Server Error` |

O DTO de erro planejado contém:

- `timestamp`: momento do erro.
- `status`: código HTTP.
- `error`: descrição do status.
- `message`: mensagem explicativa.
- `path`: caminho da requisição.
- `details`: detalhes de validação, quando aplicável.

Exemplo ilustrativo:

```json
{
  "timestamp": "2026-10-14T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não encontrado",
  "path": "/usuarios/999",
  "details": {}
}
```

O formato exato das mensagens depende do handler e das exceções implementadas.

## Configuração local

### Pré-requisitos

- JDK 25.
- Git.
- Docker Desktop (opcional, para executar em contêiner).
- Acesso a um PostgreSQL, por exemplo, uma instância no Neon.

### Variáveis de ambiente

A aplicação lê a conexão do banco pelas variáveis:

- `DB_URL`: URL JDBC do PostgreSQL.
- `DB_USERNAME`: usuário do banco.
- `DB_PASSWORD`: senha do banco.

Exemplo de configuração PowerShell (substitua pelos seus dados; nunca use os valores de exemplo em produção):

```powershell
$env:DB_URL = "jdbc:postgresql://SEU_HOST/neondb?sslmode=require"
$env:DB_USERNAME = "SEU_USUARIO"
$env:DB_PASSWORD = "SUA_SENHA"
```

A URL deve começar com `jdbc:postgresql://`. Use o host e os parâmetros fornecidos pelo provedor do banco. As variáveis definidas no PowerShell valem apenas para aquela sessão do terminal.

Configuração esperada em `src/main/resources/application.properties`:

```properties
spring.application.name=payflow

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Para produção, prefira migrações versionadas com Flyway ou Liquibase em vez de depender de `ddl-auto=update`. Também é recomendável desativar a exibição de SQL e configurar logs de forma apropriada.

### Executar localmente

No Windows/PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

A API deverá ficar disponível em `http://localhost:8080`, desde que a porta esteja livre e a conexão com o banco seja válida.

## Docker

O projeto possui um `Dockerfile` multi-stage que compila o JAR usando uma imagem JDK e executa o artefato numa imagem JRE. O arquivo `.dockerignore` exclui arquivos desnecessários do contexto de build, como `.git`, `target` e arquivos locais `.env`.

### Construir imagem

Na raiz do projeto:

```powershell
docker build -t payflow-api .
```

### Executar contêiner

Configure as variáveis de ambiente na sessão do terminal e execute:

```powershell
docker run --rm -p 8080:8080 `
  -e DB_URL `
  -e DB_USERNAME `
  -e DB_PASSWORD `
  payflow-api:latest
```

O contêiner expõe a aplicação na porta 8080 e utiliza o PostgreSQL configurado em `DB_URL`. O banco Neon continua sendo um serviço externo; não é criado um banco dentro desse contêiner.

Para interromper o processo, use `Ctrl+C`. Como foi utilizado `--rm`, o contêiner é removido ao parar, mas a imagem permanece disponível localmente.

## Testes com Postman

A coleção de testes planejada para o Postman cobre o fluxo principal:

1. Criar o remetente.
2. Criar o destinatário.
3. Depositar R$ 100.
4. Consultar e validar o saldo do remetente.
5. Transferir R$ 30.
6. Validar o saldo final do remetente (R$ 70).
7. Validar o saldo final do destinatário (R$ 30).

Os testes pressupõem que os endpoints e os nomes dos campos estejam alinhados com a implementação. Use dados de teste e evite executar repetidamente operações financeiras no banco que contém dados importantes.

### Executar com Newman

Se a coleção estiver salva no repositório em `postman/PayFlow-Postman-Tests.postman_collection.json` e o Newman estiver instalado:

```powershell
npm install -g newman
newman run .\postman\PayFlow-Postman-Tests.postman_collection.json
```

A API precisa estar rodando antes da execução. Confirme que a coleção realmente está versionada no repositório antes de usar esse comando.

## CI/CD com GitHub Actions

O workflow fica em `.github/workflows/ci.yml` na raiz do repositório.

### CI — Integração contínua

O workflow é acionado em `push` e `pull_request` para `main`. O job de teste:

1. Faz checkout do código.
2. Configura Java 25 e cache Maven.
3. Inicia um PostgreSQL temporário como serviço do job.
4. Define `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para esse banco isolado.
5. Executa `./mvnw -B verify`.

O workflow concede permissão de execução ao Maven Wrapper com `chmod +x mvnw`, necessária no runner Linux quando essa permissão não está registrada no Git.

### Publicação de imagem — planejada/em validação

O workflow também foi preparado com um job dependente do job de testes para construir e publicar a imagem no GitHub Container Registry (GHCR) apenas em push para `main`. A imagem esperada segue o padrão:

```text
ghcr.io/8rick/payflow-java:latest
ghcr.io/8rick/payflow-java:sha-<commit>
```

A publicação só deve ser considerada concluída depois de verificar que o job de publicação passou e que o pacote aparece no GitHub. A existência do workflow no repositório, isoladamente, não garante que o pacote tenha sido publicado.

## Estrutura do projeto

Estrutura conceitual esperada (os arquivos exatos devem ser confirmados no repositório):

```text
payflow-java/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .dockerignore
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src/
    ├── main/
    │   ├── java/br/com/payflow/
    │   │   ├── controllers/
    │   │   ├── dtos/
    │   │   ├── exceptions/
    │   │   ├── models/
    │   │   ├── repositories/
    │   │   └── services/
    │   └── resources/
    │       └── application.properties
    └── test/
```

## Limitações e próximos passos

### Prioridade 1 — Testes e confiabilidade

- Criar testes unitários com JUnit 5 e Mockito para cadastro, depósito e transferência.
- Testar valores zero/negativos, saldo insuficiente, usuário inexistente, CPF/e-mail duplicados e transferência para si mesmo.
- Criar testes de integração para controllers, validação, repositórios e tratamento de erros.
- Garantir que o depósito e a transferência atualizem saldos e registros de transação de forma atômica.
- Testar requisições concorrentes para evitar saldo incorreto ou gasto duplo.

### Prioridade 2 — Contrato e API

- Padronizar DTOs de resposta; evitar expor entidades JPA diretamente.
- Documentar a API com OpenAPI/Swagger.
- Adicionar endpoint de consulta de transações e histórico por usuário, se fizer parte do escopo.
- Definir paginação, ordenação e filtros para histórico.
- Revisar status HTTP e formato consistente de erros.
- Validar formato de CPF e e-mail; normalizar espaços e caixa do e-mail.

### Prioridade 3 — Banco e domínio financeiro

- Substituir `ddl-auto=update` por Flyway ou Liquibase.
- Definir precisão e escala das colunas monetárias e regras de arredondamento.
- Definir moeda e política de casas decimais.
- Considerar uma entidade/ledger de lançamentos imutáveis, em vez de depender apenas do saldo agregado.
- Definir política para idempotência de depósitos/transferências e evitar duplicidade em retentativas.
- Auditar data/hora e usar uma estratégia temporal consistente, preferencialmente UTC.

### Prioridade 4 — Segurança

- Nunca versionar credenciais, tokens, arquivos `.env` ou URLs com senha.
- Armazenar segredos de deploy nas configurações seguras da plataforma.
- Adicionar autenticação e autorização antes de expor operações financeiras publicamente.
- Não confiar apenas em IDs enviados pelo cliente para autorizar movimentações.
- Configurar CORS, limites de requisição, logs sem dados sensíveis e políticas de acesso.
- Adicionar testes de segurança e dependências vulneráveis.

### Prioridade 5 — CI/CD e operação

- Confirmar que o job de CI passa em cada push e pull request.
- Confirmar a publicação da imagem no GHCR.
- Adicionar execução de testes de API com Newman na esteira, iniciando a aplicação e esperando pelo health check.
- Publicar a imagem somente após todos os testes passarem.
- Configurar health checks e observabilidade.
- Escolher hospedagem para o contêiner, configurar variáveis secretas e fazer deploy.
- Adicionar monitoramento, logs estruturados e estratégia de rollback.
- Considerar Spring Boot Actuator para health/readiness checks.

### Prioridade 6 — Manutenção

- Adicionar instruções de contribuição e convenção de commits.
- Configurar branch protection e exigir CI verde antes de merge.
- Fixar versões de actions e revisar atualizações de dependências.
- Criar exemplos de requisições e respostas reais, após confirmar o contrato da API.




