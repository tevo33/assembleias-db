package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.client.CpfValidatorClient;
import com.cooperativismo.votacao.dto.ValidacaoCpfDTO;
import com.cooperativismo.votacao.dto.VotoDTO;
import com.cooperativismo.votacao.exception.BusinessException;
import com.cooperativismo.votacao.exception.ResourceNotFoundException;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.model.SessaoVotacao;
import com.cooperativismo.votacao.model.Voto;
import com.cooperativismo.votacao.model.Voto.OpcaoVoto;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class VotoServiceTest
{
    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private CpfValidatorClient cpfValidatorClient;

    @InjectMocks
    private VotoService votoService;

    private Pauta pauta;
    private SessaoVotacao sessaoVotacao;
    private Voto voto;
    private VotoDTO votoDTO;
    private final String cpfValido = "12345678901";

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

        voto = Voto.builder()
                   .id( 1L )
                   .pauta( pauta )
                   .cpfAssociado( cpfValido )
                   .opcaoVoto( OpcaoVoto.SIM )
                   .dataVoto( LocalDateTime.now() )
                   .build();

        votoDTO = VotoDTO.builder()
                         .pautaId( 1L )
                         .cpfAssociado( cpfValido )
                         .opcaoVoto( OpcaoVoto.SIM )
                         .build();
    }

    @Test
    @DisplayName( "Deve registrar voto com sucesso" )
    void registrarVotoComSucesso()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.of( sessaoVotacao ) );
        when( votoRepository.findByPautaIdAndCpfAssociado( 1L, cpfValido ) ).thenReturn( Optional.empty() );
        when( cpfValidatorClient.validarCpf( anyString() ) ).thenReturn( new ValidacaoCpfDTO( "ABLE_TO_VOTE" ) );
        when( votoRepository.save( any( Voto.class ) ) ).thenReturn( voto );

        VotoDTO result = votoService.registrarVoto( votoDTO );

        assertNotNull( result );
        assertEquals( voto.getId(), result.getId() );
        assertEquals( voto.getPauta().getId(), result.getPautaId() );
        assertEquals( voto.getCpfAssociado(), result.getCpfAssociado() );
        assertEquals( voto.getOpcaoVoto(), result.getOpcaoVoto() );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar votar em pauta inexistente" )
    void registrarVotoPautaInexistente()
    {
        when( pautaRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        votoDTO = VotoDTO.builder()
                         .pautaId( 99L )
                         .cpfAssociado( cpfValido )
                         .opcaoVoto( OpcaoVoto.SIM )
                         .build();

        assertThrows( ResourceNotFoundException.class, () -> 
        {
            votoService.registrarVoto( votoDTO );
        } );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar votar em pauta sem sessão de votação" )
    void registrarVotoSemSessaoVotacao()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.empty() );

        assertThrows( BusinessException.class, () -> 
        {
            votoService.registrarVoto( votoDTO );
        } );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar votar em sessão encerrada" )
    void registrarVotoSessaoEncerrada()
    {
        LocalDateTime passado = LocalDateTime.now().minusHours( 1 );
        
        sessaoVotacao = SessaoVotacao.builder()
                                     .id( sessaoVotacao.getId() )
                                     .pauta( sessaoVotacao.getPauta() )
                                     .dataAbertura( sessaoVotacao.getDataAbertura() )
                                     .dataFechamento( passado )
                                     .ativa( sessaoVotacao.isAtiva() )
                                     .build();

        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.of( sessaoVotacao ) );

        assertThrows( BusinessException.class, () ->
        {
            votoService.registrarVoto( votoDTO );
        } );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao tentar votar mais de uma vez na mesma pauta" )
    void registrarVotoDuplicado()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );
        when( sessaoVotacaoRepository.findByPautaId( 1L ) ).thenReturn( Optional.of( sessaoVotacao ) );
        when( votoRepository.findByPautaIdAndCpfAssociado( 1L, cpfValido ) ).thenReturn( Optional.of( voto ) );

        assertThrows( BusinessException.class, () -> 
        {
            votoService.registrarVoto( votoDTO );
        } );
    }
} 