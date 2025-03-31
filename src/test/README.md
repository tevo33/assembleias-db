# Testes do Sistema de Votação

- `application-test.properties`: Configurações para o ambiente de teste com banco de dados H2 em memória.
- `TestConfig.java`: Configurações de beans para teste, incluindo mocks de serviços externos.

## Como Executar os Testes

### Executar todos os testes:

```bash
mvn test
```

### Executar apenas testes unitários:

```bash
mvn test -Dtest="*ServiceTest"
```

### Executar apenas testes funcionais:

```bash
mvn test -Dtest="*IntegrationTest,VotacaoFluxoCompletoTest"
```

### Executar um teste específico:

```bash
mvn test -Dtest=PautaServiceTest
```