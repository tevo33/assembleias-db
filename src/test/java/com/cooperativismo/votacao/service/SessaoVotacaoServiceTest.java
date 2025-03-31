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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
class SessaoVotacaoServiceTest
{
    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private SessaoVotacaoService sessaoVotacaoService;

    private Pauta pauta;
    private SessaoVotacao sessaoVotacao;

    @BeforeEach
    void setUp() 
    {
        pauta = Pauta.builder()
                     .id( 1L )
                     .titulo( "Pauta de Teste" )
                     .descricao( "Descrição da pauta de teste" )
                     .build();

        LocalDateTime agora = LocalDateTime.now();

        sessaoVotacao = SessaoVotacao.builder()
                                     .id( 1L )
                                     .pauta( pauta )
                                     .dataAbertura( agora )
                                     .dataFechamento( agora.plusMinutes( 5 ) )
                                     .ativa( true )
                                     .build();
    }

    @Test
    @DisplayName( "Deve abrir sessão de votação com sucesso" )
    void abrirSessaoComSucesso()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.empty() );
        when( sessaoVotacaoRepository.save( any( SessaoVotacao.class ) ) ).thenReturn( sessaoVotacao );

        SessaoVotacaoDTO result = sessaoVotacaoService.abrirSessao( 1L, 5 );

        assertNotNull( result );
        assertEquals( sessaoVotacao.getId(), result.getId() );
        assertEquals( sessaoVotacao.getPauta().getId(), result.getPautaId() );
        assertTrue( result.isAtiva() );

        verify( sessaoVotacaoRepository, times( 1 ) ).save( any( SessaoVotacao.class ) );
    }

    @Test
    @DisplayName( "Deve abrir sessão de votação com duração padrão quando duração não especificada" )
    void abrirSessaoComDuracaoPadrao()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.empty() );
        when( sessaoVotacaoRepository.save( any( SessaoVotacao.class ) ) ).thenReturn( sessaoVotacao );

        SessaoVotacaoDTO result = sessaoVotacaoService.abrirSessao( 1L, null );

        assertNotNull( result );
        assertEquals( sessaoVotacao.getId(), result.getId() );
        assertEquals( sessaoVotacao.getPauta().getId(), result.getPautaId() );
        assertTrue( result.isAtiva() );

        verify( sessaoVotacaoRepository, times( 1 ) ).save( any( SessaoVotacao.class ) );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar abrir sessão para pauta inexistente" )
    void abrirSessaoPautaInexistente()
    {
        when( pautaRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> 
        {
            sessaoVotacaoService.abrirSessao( 99L, 5 );
        } );

        verify( sessaoVotacaoRepository, never() ).save( any( SessaoVotacao.class ) );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar abrir sessão para pauta que já possui sessão aberta" )
    void abrirSessaoPautaJaComSessao()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.of( sessaoVotacao ) );

        assertThrows( BusinessException.class, () -> 
        {
            sessaoVotacaoService.abrirSessao( 1L, 5 );
        } );

        verify( sessaoVotacaoRepository, never() ).save( any( SessaoVotacao.class ) );
    }

    @Test
    @DisplayName( "Deve obter resultado da votação com sucesso" )
    void obterResultadoComSucesso()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.of( sessaoVotacao ) );
        when( votoRepository.countByPautaId( 1L ) ).thenReturn( 10L );
        when( votoRepository.countByPautaIdAndOpcaoVoto( 1L, Voto.OpcaoVoto.SIM ) ).thenReturn( 7L );
        when( votoRepository.countByPautaIdAndOpcaoVoto( 1L, Voto.OpcaoVoto.NAO ) ).thenReturn( 3L );

        ResultadoVotacaoDTO result = sessaoVotacaoService.obterResultado( 1L );

        assertNotNull( result );
        assertEquals( pauta.getId(), result.getPautaId() );
        assertEquals( pauta.getTitulo(), result.getTituloPauta() );
        assertEquals( 10L, result.getTotalVotos() );
        assertEquals( 7L, result.getVotosSim() );
        assertEquals( 3L, result.getVotosNao() );
        assertEquals( "APROVADA", result.getResultado() );
        assertFalse( result.isSessaoEncerrada() );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar obter resultado de pauta inexistente" )
    void obterResultadoPautaInexistente()
    {
        when( pautaRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> 
        {
            sessaoVotacaoService.obterResultado( 99L );
        } );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar obter resultado de pauta sem sessão" )
    void obterResultadoPautaSemSessao()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.empty() );

        assertThrows( BusinessException.class, () ->
        {
            sessaoVotacaoService.obterResultado( 1L );
        } );
    }
} 