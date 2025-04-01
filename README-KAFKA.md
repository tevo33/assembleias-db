# Sistema de Votação com Apache Kafka

## Arquitetura

Este projeto implementa um sistema de votação para cooperativas com processamento assíncrono utilizando Apache Kafka. A arquitetura foi projetada para ser:

- **Escalável**: Pode lidar com um grande número de votos simultâneos
- **Resiliente**: Tolerante a falhas e problemas de rede
- **Performático**: Resposta rápida aos usuários através de processamento assíncrono
- **Desacoplado**: Serviços comunicam-se através de mensagens, não por chamadas diretas

## Tópicos Kafka

O sistema utiliza os seguintes tópicos Kafka:

1. **votacao-topic**: Processa votos de associados
2. **pauta-topic**: Gerencia o ciclo de vida das pautas
3. **sessao-topic**: Controla abertura e fechamento de sessões de votação
4. **resultado-topic**: Distribui resultados de votações encerradas
5. **notificacao-topic**: Envia notificações sobre eventos do sistema

## Fluxo de Funcionamento

1. **Criação de Pauta**: 
   - Controller recebe requisição para criar uma pauta
   - Serviço salva no banco e envia mensagem para `pauta-topic`
   - Consumidor processa a mensagem assincronamente

2. **Abertura de Sessão**:
   - Controller recebe requisição para abrir sessão
   - Serviço cria a sessão e envia mensagem para `sessao-topic`
   - Consumidor processa a mensagem assincronamente

3. **Registro de Voto**:
   - Controller recebe voto do associado
   - Serviço valida o voto e envia mensagem para `votacao-topic`
   - Consumidor processa o voto assincronamente e salva no banco

4. **Fechamento de Sessão**:
   - Agendador verifica periodicamente sessões expiradas
   - Fecha as sessões e envia mensagens para `sessao-topic` e `notificacao-topic`
   - Consumidor processa as mensagens e executa ações necessárias

5. **Resultado da Votação**:
   - Após o fechamento da sessão, calcula o resultado
   - Envia mensagem para `resultado-topic` e `notificacao-topic`
   - Consumidor processa as mensagens e pode executar ações como envio de emails

## Configuração Kafka

### Modo de Desenvolvimento (Single Node)

Para executar com um único nó Kafka:

```bash
docker-compose up -d
```

### Modo de Produção (Cluster)

Para executar com um cluster de 3 nós Kafka para alta disponibilidade:

```bash
docker-compose -f docker-compose-kafka-cluster.yml up -d
```

## Monitoramento

O Kafka UI está disponível em http://localhost:8090 e permite:

- Visualizar tópicos
- Monitorar consumidores
- Inspecionar mensagens
- Verificar performance do cluster

## Execução do Sistema

Para iniciar todo o sistema, use o script:

```bash
./start.bat  # Windows
./start.sh   # Linux/Mac
```

Este script:
1. Inicia o cluster Kafka
2. Cria os tópicos necessários
3. Inicia a aplicação Spring Boot

## Benefícios da Arquitetura

1. **Alta Performance**: Processamento assíncrono permite resposta rápida ao usuário
2. **Escalabilidade Horizontal**: Adicione mais instâncias de consumidores para maior capacidade
3. **Resiliência**: Mensagens são armazenadas e podem ser reprocessadas em caso de falha
4. **Separação de Responsabilidades**: Cada serviço tem uma função específica
5. **Monitoria Simplificada**: Tópicos Kafka facilitam o rastreamento de eventos

## Testando com Postman

Para testar a API usando Postman:

1. Importe a coleção Postman em `/docs/postman/votacao-api.json`
2. Execute as requisições na ordem:
   - Criar Pauta
   - Abrir Sessão
   - Registrar Voto
   - Obter Resultado
   
As mensagens serão enviadas para o Kafka e processadas assincronamente. 