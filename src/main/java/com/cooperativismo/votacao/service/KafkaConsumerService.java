package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.NotificacaoMessage;
import com.cooperativismo.votacao.dto.PautaMessage;
import com.cooperativismo.votacao.dto.ResultadoMessage;
import com.cooperativismo.votacao.dto.SessaoMessage;
import com.cooperativismo.votacao.dto.VotacaoMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService
{
    private final VotoService votoService;
    private final PautaService pautaService;
    private final SessaoVotacaoService sessaoVotacaoService;
    private final CallbackService callbackService;

    @KafkaListener(topics = "votacao-topic", groupId = "votacao-group")
    public void processarVotacao(VotacaoMessage message)
    {
        try
        {
            log.info("Processando votação recebida: {}", message);
            votoService.processarVoto(message);
            log.info("Votação processada com sucesso");
        } 
        catch (Exception e)
        {
            log.error("Erro ao processar votação: {}", message, e);
            throw new RuntimeException("Erro ao processar votação", e);
        }
    }

    @KafkaListener(topics = "pauta-topic", groupId = "votacao-group")
    public void processarPauta(PautaMessage message)
    {
        try 
        {
            log.info("Processando pauta recebida: {}", message);
            pautaService.processarPauta(message);
            log.info("Pauta processada com sucesso");
        } 
        catch (Exception e)
        {
            log.error("Erro ao processar pauta: {}", message, e);
            throw new RuntimeException("Erro ao processar pauta", e);
        }
    }

    @KafkaListener(topics = "sessao-topic", groupId = "votacao-group")
    public void processarSessao(SessaoMessage message)
    {
        try
        {
            log.info("Processando sessão recebida: {}", message);
            sessaoVotacaoService.processarSessao(message);
            log.info("Sessão processada com sucesso");
        }
        catch (Exception e)
        {
            log.error("Erro ao processar sessão: {}", message, e);
            throw new RuntimeException("Erro ao processar sessão", e);
        }
    }

    @KafkaListener(topics = "resultado-topic", groupId = "votacao-group")
    public void processarResultado(ResultadoMessage message)
    {
        try
        {
            log.info("Processando resultado recebido: {}", message);
            log.info("Resultado processado com sucesso");
        } 
        catch (Exception e)
        {
            log.error("Erro ao processar resultado: {}", message, e);
            throw new RuntimeException("Erro ao processar resultado", e);
        }
    }

    @KafkaListener(topics = "notificacao-topic", groupId = "notificacao-group")
    public void processarNotificacao( NotificacaoMessage message )
    {
        try
        {
            log.info("Processando notificação recebida: {}", message);
            
            if ("SESSAO_ENCERRADA".equals(message.getTipoNotificacao()))
            {
                log.info("Notificação de sessão encerrada recebida para pauta ID: {}", message.getPautaId());
            }
            else if ("RESULTADO_VOTACAO".equals(message.getTipoNotificacao()))
            {
                log.info("Notificação de resultado de votação recebida para pauta ID: {}", message.getPautaId());
            }
            
            log.info("Notificação processada com sucesso");
        } 
        catch (Exception e)
        {
            log.error("Erro ao processar notificação: {}", message, e);
            throw new RuntimeException("Erro ao processar notificação", e);
        }
    }
} 