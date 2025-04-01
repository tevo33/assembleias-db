package com.cooperativismo.votacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService
{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage( String topic, Object message )
    {
        try
        {
            kafkaTemplate.send( topic, message );

            log.info( "Mensagem enviada com sucesso para o tópico: {}", topic );
        }

        catch ( Exception e )
        {
            log.error("Erro ao enviar mensagem para o tópico: {}", topic, e );

            throw new RuntimeException("Erro ao enviar mensagem para o Kafka", e );
        }
    }
} 