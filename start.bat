@echo off
echo ===========================================
echo Iniciando o Sistema de Votacao com Kafka
echo ===========================================

echo 1. Iniciando o cluster Kafka...
docker-compose -f docker-compose-kafka-cluster.yml up -d

echo 2. Aguardando o Kafka iniciar (20 segundos)...
timeout /t 20 /nobreak

echo 3. Criando os topicos Kafka...
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --topic votacao-topic --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --topic pauta-topic --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --topic sessao-topic --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --topic resultado-topic --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --topic notificacao-topic --partitions 3 --replication-factor 3

echo 4. Iniciando a aplicacao...
mvn spring-boot:run

echo ===========================================
echo Sistema finalizado
echo =========================================== 