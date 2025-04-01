package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.client.CpfValidatorClient;
import com.cooperativismo.votacao.dto.VotacaoMessage;
import com.cooperativismo.votacao.dto.VotoDTO;
import com.cooperativismo.votacao.exception.ResourceNotFoundException;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.model.SessaoVotacao;
import com.cooperativismo.votacao.model.Voto;
import com.cooperativismo.votacao.repository.PautaRepository;
import com.cooperativismo.votacao.repository.SessaoVotacaoRepository;
import com.cooperativismo.votacao.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotoService 
{
    private final VotoRepository votoRepository;
    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final CpfValidatorClient cpfValidatorClient;
    private final KafkaService kafkaService;

    public void registrarVoto(VotoDTO votoDTO)
    {
        log.info("Enviando solicitação para registrar voto do associado {} na pauta {}", 
                votoDTO.getCpfAssociado(), votoDTO.getPautaId() );
        
        cpfValidatorClient.validarCpf(votoDTO.getCpfAssociado());
        
        VotacaoMessage mensagem = new VotacaoMessage(
                null,
                votoDTO.getPautaId(),
                votoDTO.getCpfAssociado(),
                votoDTO.getOpcaoVoto().toString(),
                System.currentTimeMillis());
        
        kafkaService.sendMessage("votacao-topic", mensagem);

        log.info("Mensagem enviada para o Kafka: solicitação de registro de voto para processamento");
    }
    
    @Transactional
    public void processarVoto(VotacaoMessage message)
    {
        log.info("Processando mensagem de voto: {}", message);
        
        Pauta pauta = pautaRepository.findById(message.getPautaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pauta", message.getPautaId()));
        
        Optional<SessaoVotacao> sessaoOpt = sessaoVotacaoRepository.findByPautaId(message.getPautaId());
        
        if (sessaoOpt.isEmpty()) 
        {
            log.error("Não existe sessão de votação para a pauta {}", message.getPautaId());
            return;
        }
        
        SessaoVotacao sessao = sessaoOpt.get();

        if (!sessao.estaAberta())
        {
            log.error("A sessão de votação para a pauta {} está encerrada", message.getPautaId());
            return;
        }
        
        Optional<Voto> votoExistente = votoRepository.findByPautaIdAndCpfAssociado(
                message.getPautaId(), message.getCpfAssociado());
        
        if (votoExistente.isPresent())
        {
            log.warn("O associado {} já votou na pauta {}", message.getCpfAssociado(), message.getPautaId());
            return;
        }
        
        Voto.OpcaoVoto opcaoVoto = Voto.OpcaoVoto.valueOf(message.getVoto());
        
        Voto voto = Voto.builder()
                .pauta(pauta)
                .cpfAssociado(message.getCpfAssociado())
                .opcaoVoto(opcaoVoto)
                .build();
        
        voto = votoRepository.save(voto);
        log.info("Voto registrado com ID: {} para associado: {} na pauta: {}", 
                voto.getId(), message.getCpfAssociado(), message.getPautaId());
    }

    @Transactional(readOnly = true)
    public void verificarPautaExiste(Long pautaId) {
        pautaRepository.findById(pautaId)
            .orElseThrow(() -> new ResourceNotFoundException("Pauta", pautaId));
    }

    @Transactional
    public VotoDTO registrarVotoCompleto(VotoDTO votoDTO) {
        log.info("Registrando voto completo do associado {} na pauta {}", 
                votoDTO.getCpfAssociado(), votoDTO.getPautaId());
        
        cpfValidatorClient.validarCpf(votoDTO.getCpfAssociado());
        
        verificarPautaExiste(votoDTO.getPautaId());
        
        VotacaoMessage mensagem = new VotacaoMessage(
                null,
                votoDTO.getPautaId(),
                votoDTO.getCpfAssociado(),
                votoDTO.getOpcaoVoto().toString(),
                System.currentTimeMillis());
        
        kafkaService.sendMessage("votacao-topic", mensagem);
        log.info("Mensagem enviada para o Kafka: solicitação de registro de voto");
        
        return votoDTO;
    }
} 