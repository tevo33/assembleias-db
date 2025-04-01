package com.cooperativismo.votacao.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig
{
    @Value( "${spring.kafka.bootstrap-servers}" )
    private String bootstrapServers;

    @Bean
    public NewTopic votacaoTopic()
    {
        return TopicBuilder.name( "votacao-topic" )
                           .partitions( 3 )
                           .replicas( 1 )
                           .build();
    }

    @Bean
    public NewTopic pautaTopic()
    {
        return TopicBuilder.name( "pauta-topic" )
                           .partitions(3 )
                           .replicas(1 )
                           .build();
    }

    @Bean
    public NewTopic sessaoTopic()
    {
        return TopicBuilder.name( "sessao-topic" )
                           .partitions(3 )
                           .replicas(1 )
                           .build();
    }

    @Bean
    public NewTopic resultadoTopic()
    {
        return TopicBuilder.name( "resultado-topic" )
                           .partitions( 3 )
                           .replicas( 1 )
                           .build();
    }

    @Bean
    public NewTopic notificacaoTopic()
    {
        return TopicBuilder.name( "notificacao-topic" )
                           .partitions( 3 )
                           .replicas( 1 )
                           .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory()
    {
        Map<String, Object> configProps = new HashMap<>();
    
        configProps.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        configProps.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        configProps.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class );
    
        return new DefaultKafkaProducerFactory<>( configProps );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate()
    {
        return new KafkaTemplate<>( producerFactory() );
    }
} 