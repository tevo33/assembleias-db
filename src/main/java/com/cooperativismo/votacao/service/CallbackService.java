package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.config.CallbackConfig;
import com.cooperativismo.votacao.dto.ResultadoVotacaoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService
{
    private final CallbackConfig callbackConfig;
    private final RestTemplate restTemplate;

    public void notificarSessaoEncerrada( Long sessaoId, Long pautaId )
    {
        if ( ! callbackConfig.isEnabled() )
        {
            log.debug( "Callbacks desabilitados. Ignorando notificação de sessão encerrada");
            
            return;
        }

        String url = callbackConfig.getFullUrl( "sessao-encerrada" );
        
        if ( url == null )
        {
            log.warn( "URL de callback para sessão encerrada não configurada" );
        
            return;
        }

        try
        {
            log.info( "Enviando notificação de sessão encerrada para: {}", url );
            
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType( MediaType.APPLICATION_JSON );
            
            String payload = String.format(  "{\"sessaoId\":%d,\"pautaId\":%d,\"evento\":\"SESSAO_ENCERRADA\"}", 
                                          sessaoId, pautaId );
            
            HttpEntity<String> request = new HttpEntity<>( payload, headers );
            
            restTemplate.postForEntity( url, request, String.class );
            
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

        String url = callbackConfig.getFullUrl("resultado-votacao");
        
        if ( url == null )
        {
            log.warn("URL de callback para resultado de votação não configurada");

            return;
        }

        try
        {
            log.info( "Enviando notificação de resultado de votação para: {}", url );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType( MediaType.APPLICATION_JSON );
            
            HttpEntity<ResultadoVotacaoDTO> request = new HttpEntity<>( resultado, headers );
            restTemplate.postForEntity( url, request, String.class );
            
            log.info("Notificação de resultado de votação enviada com sucesso");
        }
        
        catch ( Exception e )
        {
            log.error( "Erro ao enviar notificação de resultado de votação: {}", e.getMessage(), e );
        }
    }
} 