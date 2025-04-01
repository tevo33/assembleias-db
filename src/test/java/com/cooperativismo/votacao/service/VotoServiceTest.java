package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.client.CpfValidatorClient;
import com.cooperativismo.votacao.dto.ValidacaoCpfDTO;
import com.cooperativismo.votacao.dto.VotacaoMessage;
import com.cooperativismo.votacao.dto.VotoDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    
    @Mock
    private KafkaService kafkaService;

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
                     .id(1L)
                     .titulo("Pauta de Teste")
                     .descricao("Descrição da pauta de teste")
                     .build();

        LocalDateTime agora = LocalDateTime.now();

        sessaoVotacao = SessaoVotacao.builder()
                                     .id(1L)
                                     .pauta(pauta)
                                     .dataAbertura(agora)
                                     .dataFechamento(agora.plusMinutes(5))
                                     .ativa(true)
                                     .build();

        voto = Voto.builder()
                   .id(1L)
                   .pauta(pauta)
                   .cpfAssociado(cpfValido)
                   .opcaoVoto(OpcaoVoto.SIM)
                   .dataVoto(LocalDateTime.now())
                   .build();

        votoDTO = VotoDTO.builder()
                         .pautaId(1L)
                         .cpfAssociado(cpfValido)
                         .opcaoVoto(OpcaoVoto.SIM)
                         .build();
    }

    @Test
    @DisplayName("Deve enviar mensagem para registrar voto de forma assíncrona")
    void registrarVoto()
    {
        when(cpfValidatorClient.validarCpf(anyString())).thenReturn(new ValidacaoCpfDTO("ABLE_TO_VOTE"));
        doNothing().when(kafkaService).sendMessage(eq("votacao-topic"), any(VotacaoMessage.class));
        
        votoService.registrarVoto(votoDTO);
        
        verify(cpfValidatorClient, times(1)).validarCpf(anyString());
        verify(kafkaService, times(1)).sendMessage(eq("votacao-topic"), any(VotacaoMessage.class));
    }
    
    @Test
    @DisplayName("Deve processar mensagem de voto com sucesso")
    void processarVotoComSucesso()
    {
        VotacaoMessage message = new VotacaoMessage(
                                null,
                                1L,
                                cpfValido,
                                "SIM",
                                System.currentTimeMillis()
                             );
                            
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoVotacao));
        when(votoRepository.findByPautaIdAndCpfAssociado(1L, cpfValido)).thenReturn(Optional.empty());
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);
        
        votoService.processarVoto(message);
        
        verify(pautaRepository, times(1)).findById(1L);
        verify(sessaoVotacaoRepository, times(1)).findByPautaId(1L);
        verify(votoRepository, times(1)).findByPautaIdAndCpfAssociado(1L, cpfValido);
        verify(votoRepository, times(1)).save(any(Voto.class));
    }
    
    @Test
    @DisplayName("Não deve processar voto quando pauta não existe")
    void processarVotoPautaInexistente()
    {
        VotacaoMessage message = new VotacaoMessage(
                                null,
                                99L,
                                cpfValido,
                                "SIM",
                                System.currentTimeMillis()
                             );
                            
        when(pautaRepository.findById(99L)).thenThrow(new ResourceNotFoundException("Pauta", 99L));
        
        assertThrows(ResourceNotFoundException.class, () -> 
        {
            votoService.processarVoto( message );
        } );
        
        verify( votoRepository, never() ).save( any( Voto.class ) );
    }
    
    @Test
    @DisplayName("Não deve processar voto quando sessão não existe")
    void processarVotoSemSessaoVotacao()
    {
        VotacaoMessage message = new VotacaoMessage(
                                null,
                                1L,
                                cpfValido,
                                "SIM",
                                System.currentTimeMillis()
                             );
                            
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());
        
        votoService.processarVoto(message);
        
        verify(votoRepository, never()).save(any(Voto.class));
    }
    
    @Test
    @DisplayName("Não deve processar voto quando sessão está encerrada")
    void processarVotoSessaoEncerrada()
    {
        VotacaoMessage message = new VotacaoMessage(
                                null,
                                1L,
                                cpfValido,
                                "SIM",
                                System.currentTimeMillis()
                             );
        
        LocalDateTime passado = LocalDateTime.now().minusHours(1);
        
        SessaoVotacao sessaoEncerrada = SessaoVotacao.builder()
                                     .id(sessaoVotacao.getId())
                                     .pauta(sessaoVotacao.getPauta())
                                     .dataAbertura(sessaoVotacao.getDataAbertura())
                                     .dataFechamento(passado)
                                     .ativa(sessaoVotacao.isAtiva())
                                     .build();
                            
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoEncerrada));
        
        votoService.processarVoto(message);
        
        verify(votoRepository, never()).save(any(Voto.class));
    }
    
    @Test
    @DisplayName("Não deve processar voto duplicado")
    void processarVotoDuplicado()
    {
        VotacaoMessage message = new VotacaoMessage(
                                null,
                                1L,
                                cpfValido,
                                "SIM",
                                System.currentTimeMillis()
                             );
                            
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoVotacao));
        when(votoRepository.findByPautaIdAndCpfAssociado(1L, cpfValido)).thenReturn(Optional.of(voto));
        
        votoService.processarVoto(message);
        
        verify(votoRepository, never()).save(any(Voto.class));
    }
} 