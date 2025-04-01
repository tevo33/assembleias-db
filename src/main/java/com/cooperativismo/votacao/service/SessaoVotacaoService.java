package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.NotificacaoMessage;
import com.cooperativismo.votacao.dto.ResultadoMessage;
import com.cooperativismo.votacao.dto.ResultadoVotacaoDTO;
import com.cooperativismo.votacao.dto.SessaoMessage;
import com.cooperativismo.votacao.exception.BusinessException;
import com.cooperativismo.votacao.exception.ResourceNotFoundException;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.model.SessaoVotacao;
import com.cooperativismo.votacao.model.Voto;
import com.cooperativismo.votacao.repository.PautaRepository;
import com.cooperativismo.votacao.repository.SessaoVotacaoRepository;
import com.cooperativismo.votacao.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessaoVotacaoService
{
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;
    private final CallbackService callbackService;
    private final KafkaService kafkaService;

    public void abrirSessao(Long pautaId, Integer duracaoMinutos)
    {
        log.info("Enviando solicitação para abrir sessão de votação para pauta ID: {} com duração de {} minutos", pautaId, duracaoMinutos);
        
        int duracao = duracaoMinutos != null && duracaoMinutos > 0 ? duracaoMinutos : 1;
        
        LocalDateTime dataAbertura = LocalDateTime.now();
        LocalDateTime dataFechamento = dataAbertura.plusMinutes(duracao);
        
        SessaoMessage message = new SessaoMessage(
            null,
            pautaId,
            dataAbertura,
            dataFechamento,
            "ABRIR",
            System.currentTimeMillis()
        );
        
        kafkaService.sendMessage("sessao-topic", message);
        log.info("Mensagem enviada para o Kafka: solicitação para abrir sessão para pauta ID {}", pautaId);
    }
    
    @Transactional(readOnly = true)
    public ResultadoVotacaoDTO obterResultado(Long pautaId)
    {
        log.info("Obtendo resultado da votação para pauta ID: {}", pautaId);
        
        Pauta pauta = pautaRepository.findById(pautaId).orElseThrow(() -> new ResourceNotFoundException("Pauta", pautaId));
        
        Optional<SessaoVotacao> sessaoOpt = sessaoVotacaoRepository.findByPautaId(pautaId);
        
        if (sessaoOpt.isEmpty())
        {
            throw new BusinessException("Não existe sessão de votação para esta pauta");
        }
        
        SessaoVotacao sessao = sessaoOpt.get();
        boolean sessaoEncerrada = !sessao.estaAberta();
        
        long totalVotos = votoRepository.countByPautaId(pautaId);
        long votosSim = votoRepository.countByPautaIdAndOpcaoVoto(pautaId, Voto.OpcaoVoto.SIM);
        long votosNao = votoRepository.countByPautaIdAndOpcaoVoto(pautaId, Voto.OpcaoVoto.NAO);
        
        String resultado = getResultadoVotacao(votosSim, votosNao);
        
        ResultadoVotacaoDTO resultadoDTO = ResultadoVotacaoDTO.builder()
                                  .pautaId(pautaId)
                                  .tituloPauta(pauta.getTitulo())
                                  .totalVotos(totalVotos)
                                  .votosSim(votosSim)
                                  .votosNao(votosNao)
                                  .resultado(resultado)
                                  .sessaoEncerrada(sessaoEncerrada)
                                  .build();
        
        if (sessaoEncerrada) 
        {
            ResultadoMessage resultadoMessage = new ResultadoMessage(
                pautaId,
                pauta.getTitulo(),
                totalVotos,
                votosSim,
                votosNao,
                resultado,
                System.currentTimeMillis()
            );
            
            kafkaService.sendMessage("resultado-topic", resultadoMessage);
            log.info("Mensagem enviada para o Kafka: resultado da votação para pauta ID {}", pautaId);
            
            callbackService.notificarResultadoVotacao(resultadoDTO);
        }
        
        return resultadoDTO;
    }
    
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void verificarSessoesExpiradas()
    {
        log.info("Verificando sessões de votação expiradas");
        
        List<SessaoVotacao> sessoesExpiradas = sessaoVotacaoRepository.findSessoesAtivasExpiradas(LocalDateTime.now());
        
        for (SessaoVotacao sessao : sessoesExpiradas)
        {
            log.info("Enviando solicitação para fechar sessão de votação expirada para pauta ID: {}", sessao.getPauta().getId());
            
            SessaoMessage sessaoMessage = new SessaoMessage(
                sessao.getId(),
                sessao.getPauta().getId(),
                sessao.getDataAbertura(),
                sessao.getDataFechamento(),
                "FECHAR",
                System.currentTimeMillis()
            );
            
            kafkaService.sendMessage("sessao-topic", sessaoMessage);
            log.info("Mensagem enviada para o Kafka: solicitação para fechar sessão para pauta ID {}", sessao.getPauta().getId());
        }
    }
    
    @Transactional
    public void processarSessao(SessaoMessage message) {
        log.info("Processando mensagem de sessão: {}", message);
        
        if ("ABRIR".equals(message.getTipoOperacao())) {
            log.info("Abrindo sessão para pauta ID: {}", message.getPautaId());
            
            Pauta pauta = pautaRepository.findById(message.getPautaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pauta", message.getPautaId()));
            
            if (sessaoVotacaoRepository.findByPautaId(message.getPautaId()).isPresent()) {
                log.warn("Já existe uma sessão de votação aberta para esta pauta: {}", message.getPautaId());
                return;
            }
            
            SessaoVotacao sessaoVotacao = SessaoVotacao.builder()
                                                    .pauta(pauta)
                                                    .dataAbertura(message.getDataAbertura())
                                                    .dataFechamento(message.getDataFechamento())
                                                    .ativa(true)
                                                    .build();
            
            sessaoVotacao = sessaoVotacaoRepository.save(sessaoVotacao);
            log.info("Sessão aberta com ID: {} para pauta ID: {}", sessaoVotacao.getId(), message.getPautaId());
            
        } else if ("FECHAR".equals(message.getTipoOperacao())) {
            log.info("Fechando sessão para ID: {}", message.getSessaoId());
            
            sessaoVotacaoRepository.findById(message.getSessaoId())
                .ifPresent(sessao -> {
                    try {
                        sessao.fechar();
                        sessaoVotacaoRepository.save(sessao);
                        log.info("Sessão fechada com ID: {}", message.getSessaoId());
                        
                        NotificacaoMessage notificacaoMessage = new NotificacaoMessage(
                            sessao.getPauta().getId(),
                            sessao.getId(),
                            "SESSAO_ENCERRADA",
                            message,
                            System.currentTimeMillis()
                        );
                        
                        kafkaService.sendMessage("notificacao-topic", notificacaoMessage);
                        
                        try {
                            callbackService.notificarSessaoEncerrada(sessao.getId(), sessao.getPauta().getId());
                        } catch (Exception e) {
                            log.error("Erro ao notificar encerramento da sessão via callback, mas continuando processamento: {}", e.getMessage());
                        }
                        
                        try {
                            ResultadoVotacaoDTO resultado = obterResultado(sessao.getPauta().getId());
                            callbackService.notificarResultadoVotacao(resultado);
                        } catch (Exception e) {
                            log.error("Erro ao notificar resultado da votação via callback, mas continuando processamento: {}", e.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("Erro ao processar fechamento de sessão: {}", e.getMessage(), e);
                    }
                });
        }
    }

    private String getResultadoVotacao(long votosSim, long votosNao)
    {
        if (votosSim > votosNao)
        {
            return "APROVADA";
        } 
        
        else if (votosNao > votosSim)
        {
            return "REJEITADA";
        }
        
        return "EMPATE";
    }

    @Transactional(readOnly = true)
    public void verificarPautaExiste( Long pautaId )
    {
        pautaRepository.findById( pautaId ).orElseThrow( () -> new ResourceNotFoundException( "Pauta", pautaId ) );
    }

    @Transactional
    public SessaoVotacao abrirSessaoCompleta(Long pautaId, Integer duracaoMinutos) {
        log.info("Abrindo sessão de votação para pauta ID: {} com duração de {} minutos", pautaId, duracaoMinutos);
        
        int duracao = duracaoMinutos != null && duracaoMinutos > 0 ? duracaoMinutos : 1;
        
        LocalDateTime dataAbertura = LocalDateTime.now();
        LocalDateTime dataFechamento = dataAbertura.plusMinutes(duracao);
        
        verificarPautaExiste(pautaId);
        
        SessaoMessage message = new SessaoMessage(
            null,
            pautaId,
            dataAbertura,
            dataFechamento,
            "ABRIR",
            System.currentTimeMillis()
        );
        
        kafkaService.sendMessage("sessao-topic", message);
        log.info("Mensagem enviada para o Kafka: solicitação para abrir sessão para pauta ID {}", pautaId);
        
        return SessaoVotacao.builder()
                .pauta(pautaRepository.findById(pautaId).get())
                .dataAbertura(dataAbertura)
                .dataFechamento(dataFechamento)
                .ativa(true)
                .build();
    }
} 