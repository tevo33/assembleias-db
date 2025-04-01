package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.PautaDTO;
import com.cooperativismo.votacao.dto.PautaMessage;
import com.cooperativismo.votacao.exception.ResourceNotFoundException;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.repository.PautaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PautaService
{
    private final PautaRepository pautaRepository;
    private final KafkaService kafkaService;

    @Transactional( readOnly = true )
    public List<PautaDTO> listarPautas() 
    {
        log.info("Listando todas as pautas");

        return pautaRepository.findAll().stream().map( PautaDTO::convertToDto ).collect( Collectors.toList() );
    }

    @Transactional(readOnly = true)
    public PautaDTO buscarPauta(Long id) 
    {
        log.info( "Buscando pauta com ID: {}", id );

        Pauta pauta = pautaRepository.findById(id).orElseThrow( () -> new ResourceNotFoundException( "Pauta", id ) );

        return PautaDTO.convertToDto( pauta );
    }

    @Transactional
    public PautaDTO criarPautaCompleta(PautaDTO pautaDTO)
    {
        log.info("Enviando solicitação para criar pauta: {}", pautaDTO.getTitulo());
        
        PautaMessage message = new PautaMessage(
                null,
                pautaDTO.getTitulo(),
                pautaDTO.getDescricao(),
                "CREATE",
                System.currentTimeMillis()
        );
        
        kafkaService.sendMessage( "pauta-topic", message );
        log.info( "Mensagem enviada para o Kafka: solicitação de criação de pauta" );
        
        return pautaDTO;
    }

    public void criarPauta( PautaDTO pautaDTO )
    {
        log.info( "Enviando solicitação para criar pauta: {}", pautaDTO.getTitulo() );
    
        PautaMessage message = new PautaMessage( null,
                                 pautaDTO.getTitulo(),
                                 pautaDTO.getDescricao(),
                                 "CREATE",
                                 System.currentTimeMillis());
        
        kafkaService.sendMessage( "pauta-topic", message );
        log.info( "Mensagem enviada para o Kafka: solicitação de criação de pauta" );
    }
    
    @Transactional
    public void processarPauta( PautaMessage message )
    {
        log.info("Processando mensagem de pauta: {}", message );
        
        if ("CREATE".equals( message.getTipoOperacao() ) )
        {
            log.info( "Criando pauta: {}", message.getTitulo() );
            
            Pauta pauta = Pauta.builder()
                               .titulo( message.getTitulo() )
                               .descricao( message.getDescricao() )
                               .build();
            
            pauta = pautaRepository.save( pauta );

            log.info( "Pauta criada com ID: {}", pauta.getId() );
        } 
    }
} 