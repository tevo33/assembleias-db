package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.ResultadoVotacaoDTO;
import com.cooperativismo.votacao.dto.SessaoVotacaoDTO;
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

    @Transactional
    public SessaoVotacaoDTO abrirSessao( Long pautaId, Integer duracaoMinutos )
    {
        log.info( "Abrindo sessão de votação para pauta ID: {} com duração de {} minutos", pautaId, duracaoMinutos );
        
        Pauta pauta = pautaRepository.findById( pautaId ).orElseThrow( () -> new ResourceNotFoundException( "Pauta", pautaId ) );
        
        if ( sessaoVotacaoRepository.findByPautaId( pautaId ).isPresent() )
        {
            throw new BusinessException("Já existe uma sessão de votação aberta para esta pauta" );
        }
        
        int duracao = duracaoMinutos != null && duracaoMinutos > 0 ? duracaoMinutos : 1;
        
        LocalDateTime dataAbertura = LocalDateTime.now();
        LocalDateTime dataFechamento = dataAbertura.plusMinutes( duracao );
        
        SessaoVotacao sessaoVotacao = SessaoVotacao.builder()
                                                   .pauta( pauta )
                                                   .dataAbertura( dataAbertura )
                                                   .dataFechamento( dataFechamento )
                                                   .ativa( true )
                                                   .build();
        
        sessaoVotacao = sessaoVotacaoRepository.save( sessaoVotacao );
        
        return convertToDto( sessaoVotacao );
    }
    
    @Transactional( readOnly = true )
    public ResultadoVotacaoDTO obterResultado( Long pautaId )
    {
        log.info( "Obtendo resultado da votação para pauta ID: {}", pautaId );
        
        Pauta pauta = pautaRepository.findById( pautaId ).orElseThrow( () -> new ResourceNotFoundException( "Pauta", pautaId ) );
        
        Optional<SessaoVotacao> sessaoOpt = sessaoVotacaoRepository.findByPautaId( pautaId );
        
        if ( sessaoOpt.isEmpty() )
        {
            throw new BusinessException( "Não existe sessão de votação para esta pauta" );
        }
        
        SessaoVotacao sessao = sessaoOpt.get();
        boolean sessaoEncerrada = ! sessao.estaAberta();
        
        long totalVotos = votoRepository.countByPautaId( pautaId );
        long votosSim   = votoRepository.countByPautaIdAndOpcaoVoto( pautaId, Voto.OpcaoVoto.SIM );
        long votosNao   = votoRepository.countByPautaIdAndOpcaoVoto( pautaId, Voto.OpcaoVoto.NAO );
        
        String resultado;

        if ( votosSim > votosNao ) 
        {
            resultado = "APROVADA";
        } 
        
        else if ( votosNao > votosSim )
        {
            resultado = "REJEITADA";
        } 
        
        else 
        {
            resultado = "EMPATE";
        }
        
        ResultadoVotacaoDTO resultadoDTO = ResultadoVotacaoDTO.builder()
                                  .pautaId( pautaId )
                                  .tituloPauta( pauta.getTitulo() )
                                  .totalVotos( totalVotos )
                                  .votosSim( votosSim )
                                  .votosNao( votosNao )
                                  .resultado( resultado )
                                  .sessaoEncerrada( sessaoEncerrada )
                                  .build();
        
        if ( sessaoEncerrada ) 
        {
            callbackService.notificarResultadoVotacao(resultadoDTO);
        }
        
        return resultadoDTO;
    }
    
    @Scheduled( fixedRate = 60000 )
    @Transactional
    public void verificarSessoesExpiradas()
    {
        log.info( "Verificando sessões de votação expiradas" );
        
        List<SessaoVotacao> sessoesExpiradas = sessaoVotacaoRepository.findSessoesAtivasExpiradas( LocalDateTime.now() );
        
        for ( SessaoVotacao sessao : sessoesExpiradas )
        {
            log.info(  "Fechando sessão de votação expirada para pauta ID: {}", sessao.getPauta().getId() );
        
            sessao.fechar();
        
            sessaoVotacaoRepository.save( sessao );
            
            callbackService.notificarSessaoEncerrada( sessao.getId(), sessao.getPauta().getId() );
            
            ResultadoVotacaoDTO resultado = obterResultado( sessao.getPauta().getId() );
            callbackService.notificarResultadoVotacao( resultado );
        }
    }
    
    private SessaoVotacaoDTO convertToDto( SessaoVotacao sessaoVotacao )
    {
        long duracaoSegundos = java.time.Duration.between( sessaoVotacao.getDataAbertura(),
                                                           sessaoVotacao.getDataFechamento() ).getSeconds();

        int duracaoMinutos = (int) Math.max( 1, Math.ceil( duracaoSegundos / 60.0 ) );
        
        return SessaoVotacaoDTO.builder()
                               .id( sessaoVotacao.getId() )
                               .duracaoMinutos( duracaoMinutos )
                               .pautaId( sessaoVotacao.getPauta().getId() )
                               .dataAbertura( sessaoVotacao.getDataAbertura() )
                               .dataFechamento( sessaoVotacao.getDataFechamento() )
                               .ativa( sessaoVotacao.isAtiva() )
                               .build();
    }
} 