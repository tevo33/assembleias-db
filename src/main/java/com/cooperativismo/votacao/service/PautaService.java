package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.PautaDTO;
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

    @Transactional( readOnly = true )
    public List<PautaDTO> listarPautas() 
    {
        log.info("Listando todas as pautas" );

        return pautaRepository.findAll().stream().map( this::convertToDto ).collect( Collectors.toList() );
    }

    @Transactional( readOnly = true )
    public PautaDTO buscarPauta( Long id ) 
    {
        log.info( "Buscando pauta com ID: {}", id );
        Pauta pauta = pautaRepository.findById( id ).orElseThrow( () -> new ResourceNotFoundException( "Pauta", id ) );

        return convertToDto( pauta );
    }

    @Transactional
    public PautaDTO criarPauta( PautaDTO pautaDTO )
    {
        log.info( "Criando pauta: {}", pautaDTO.getTitulo() );
    
        Pauta pauta = convertToEntity( pautaDTO );
        pauta = pautaRepository.save( pauta );
    
        return convertToDto( pauta );
    }

    private PautaDTO convertToDto( Pauta pauta )
    {
        return PautaDTO.builder()
                       .id( pauta.getId() )
                       .titulo( pauta.getTitulo() )
                       .descricao( pauta.getDescricao() )
                       .build();
    }

    private Pauta convertToEntity( PautaDTO pautaDTO )
    {
        return Pauta.builder()
                    .titulo(pautaDTO.getTitulo())
                    .descricao(pautaDTO.getDescricao())
                    .build();
    }
} 