package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.config.CallbackConfig;
import com.cooperativismo.votacao.dto.NotificacaoMessage;
import com.cooperativismo.votacao.dto.ResultadoVotacaoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService
{
    private final CallbackConfig callbackConfig;
    private final KafkaService kafkaService;

    public void notificarSessaoEncerrada( Long sessaoId, Long pautaId )
    {
        if ( ! callbackConfig.isEnabled() )
        {
            log.debug( "Callbacks desabilitados. Ignorando notificação de sessão encerrada");
            
            return;
        }

        try
        {
            log.info( "Enviando notificação de sessão encerrada via Kafka" );
            
            NotificacaoMessage mensagem = new NotificacaoMessage(
                pautaId,
                sessaoId,
                "SESSAO_ENCERRADA",
                null,
                Instant.now().toEpochMilli()
            );
            
            kafkaService.sendMessage("notificacao-topic", mensagem);
            
            log.info( "Notificação de sessão encerrada enviada com sucesso" );
        } 
        
        catch ( Exception e ) 
        {
            log.error( "Erro ao enviar notificação de sessão encerrada: {}", e.getMessage(), e );
        }
    }

    public void notificarResultadoVotacao( ResultadoVotacaoDTO resultado )
    {
        if ( ! callbackConfig.isEnabled() )
        {
            log.debug( "Callbacks desabilitados. Ignorando notificação de resultado de votação" );
            
            return;
        }

        try
        {
            log.info( "Enviando notificação de resultado de votação via Kafka" );
            
            NotificacaoMessage mensagem = new NotificacaoMessage(
                resultado.getPautaId(),
                null,
                "RESULTADO_VOTACAO",
                resultado,
                Instant.now().toEpochMilli()
            );
            
            kafkaService.sendMessage("notificacao-topic", mensagem);
            
            log.info("Notificação de resultado de votação enviada com sucesso");
        }
        
        catch ( Exception e )
        {
            log.error( "Erro ao enviar notificação de resultado de votação: {}", e.getMessage(), e );
        }
    }
} 