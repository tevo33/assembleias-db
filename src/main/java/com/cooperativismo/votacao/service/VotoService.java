package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.client.CpfValidatorClient;
import com.cooperativismo.votacao.dto.VotoDTO;
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

    @Transactional
    public VotoDTO registrarVoto( VotoDTO votoDTO )
    {
        log.info( "Registrando voto do associado {} na pauta {}", votoDTO.getCpfAssociado(), votoDTO.getPautaId() );
        
        Pauta pauta = pautaRepository.findById( votoDTO.getPautaId() )
                                     .orElseThrow( () -> new ResourceNotFoundException( "Pauta", votoDTO.getPautaId() ) );
        
        Optional<SessaoVotacao> sessaoOpt = sessaoVotacaoRepository.findByPautaId( votoDTO.getPautaId() );
        
        if ( sessaoOpt.isEmpty() ) 
        {
            throw new BusinessException( "Não existe sessão de votação aberta para esta pauta" );
        }
        
        SessaoVotacao sessao = sessaoOpt.get();

        if ( ! sessao.estaAberta() )
        {
            throw new BusinessException( "A sessão de votação está encerrada" );
        }
        
        Optional<Voto> votoExistente = votoRepository.findByPautaIdAndCpfAssociado( votoDTO.getPautaId(), votoDTO.getCpfAssociado());
        
        if ( votoExistente.isPresent() )
        {
            throw new BusinessException( "O associado já votou nesta pauta" );
        }
        
        cpfValidatorClient.validarCpf( votoDTO.getCpfAssociado() );
        
        Voto voto = Voto.builder()
                        .pauta( pauta )
                        .cpfAssociado( votoDTO.getCpfAssociado() )
                        .opcaoVoto( votoDTO.getOpcaoVoto() )
                        .build();
        
        voto = votoRepository.save( voto );
        
        return convertToDto( voto );
    }
    
    private VotoDTO convertToDto( Voto voto )
    {
        return VotoDTO.builder()
                      .id( voto.getId() )
                      .pautaId( voto.getPauta().getId() )
                      .cpfAssociado( voto.getCpfAssociado() )
                      .opcaoVoto( voto.getOpcaoVoto() )
                      .build();
    }
} 