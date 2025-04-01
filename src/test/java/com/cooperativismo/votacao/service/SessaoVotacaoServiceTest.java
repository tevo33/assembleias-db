package com.cooperativismo.votacao.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoServiceTest
{
    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;
    
    @Mock
    private CallbackService callbackService;
    
    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private SessaoVotacaoService sessaoVotacaoService;

    private Pauta pauta;
    private SessaoVotacao sessaoVotacao;
    private LocalDateTime agora;

    @BeforeEach
    void setUp() 
    {
        agora = LocalDateTime.now();
        
        pauta = Pauta.builder()
                     .id(1L)
                     .titulo("Pauta de Teste")
                     .descricao("Descrição da pauta de teste")
                     .build();

        sessaoVotacao = SessaoVotacao.builder()
                                     .id(1L)
                                     .pauta(pauta)
                                     .dataAbertura(agora)
                                     .dataFechamento(agora.plusMinutes(5))
                                     .ativa(true)
                                     .build();
    }

    @Test
    @DisplayName("Deve enviar mensagem para abrir sessão de votação")
    void abrirSessaoComSucesso()
    {
        doNothing().when(kafkaService).sendMessage(eq("sessao-topic"), any(SessaoMessage.class));

        sessaoVotacaoService.abrirSessao(1L, 5);

        verify(kafkaService, times(1)).sendMessage(eq("sessao-topic"), any(SessaoMessage.class));
    }

    @Test
    @DisplayName("Deve processar mensagem de abertura de sessão")
    void processarSessaoAbrir()
    {
        SessaoMessage message = new SessaoMessage(
                                null,
                                1L,
                                agora,
                                agora.plusMinutes(5),
                                "ABRIR",
                                System.currentTimeMillis()
                            );
                            
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessaoVotacao);
        
        sessaoVotacaoService.processarSessao(message);
        
        verify(pautaRepository, times(1)).findById(1L);
        verify(sessaoVotacaoRepository, times(1)).findByPautaId(1L);
        verify(sessaoVotacaoRepository, times(1)).save(any(SessaoVotacao.class));
    }
    
    @Test
    @DisplayName("Não deve abrir sessão se pauta já possui sessão aberta")
    void processarSessaoAbrirPautaJaComSessao()
    {
        SessaoMessage message = new SessaoMessage(
                                null,
                                1L,
                                agora,
                                agora.plusMinutes(5),
                                "ABRIR",
                                System.currentTimeMillis()
                            );
                            
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoVotacao));
        
        sessaoVotacaoService.processarSessao(message);
        
        verify(pautaRepository, times(1)).findById(1L);
        verify(sessaoVotacaoRepository, times(1)).findByPautaId(1L);
        verify(sessaoVotacaoRepository, never()).save(any(SessaoVotacao.class));
    }
    
    @Test
    @DisplayName("Deve processar mensagem de fechamento de sessão")
    void processarSessaoFechar()
    {
        SessaoMessage message = new SessaoMessage(
                                1L,
                                1L,
                                agora,
                                agora.plusMinutes(5),
                                "FECHAR",
                                System.currentTimeMillis()
                            );
                            
        when(sessaoVotacaoRepository.findById(1L)).thenReturn(Optional.of(sessaoVotacao));
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessaoVotacao);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoVotacao));
        when(votoRepository.countByPautaId(1L)).thenReturn(10L);
        when(votoRepository.countByPautaIdAndOpcaoVoto(1L, Voto.OpcaoVoto.SIM)).thenReturn(7L);
        when(votoRepository.countByPautaIdAndOpcaoVoto(1L, Voto.OpcaoVoto.NAO)).thenReturn(3L);
        doNothing().when(callbackService).notificarSessaoEncerrada(any(Long.class), any(Long.class));
        doNothing().when(callbackService).notificarResultadoVotacao(any(ResultadoVotacaoDTO.class));
        doNothing().when(kafkaService).sendMessage(anyString(), any());
        
        sessaoVotacaoService.processarSessao(message);
        
        verify(sessaoVotacaoRepository, times(1)).findById(1L);
        verify(sessaoVotacaoRepository, times(1)).save(any(SessaoVotacao.class));
        verify(kafkaService, times(1)).sendMessage(eq("notificacao-topic"), any());
    }
    
    @Test
    @DisplayName("Deve verificar sessões expiradas e enviar mensagens para fechá-las")
    void verificarSessoesExpiradas()
    {
        when(sessaoVotacaoRepository.findSessoesAtivasExpiradas(any(LocalDateTime.class)))
            .thenReturn(Collections.singletonList(sessaoVotacao));
        doNothing().when(kafkaService).sendMessage(eq("sessao-topic"), any(SessaoMessage.class));
        
        sessaoVotacaoService.verificarSessoesExpiradas();
        
        verify(sessaoVotacaoRepository, times(1)).findSessoesAtivasExpiradas(any(LocalDateTime.class));
        verify(kafkaService, times(1)).sendMessage(eq("sessao-topic"), any(SessaoMessage.class));
    }

    @Test
    @DisplayName("Deve obter resultado da votação com sucesso")
    void obterResultadoComSucesso()
    {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoVotacao));
        when(votoRepository.countByPautaId(1L)).thenReturn(10L);
        when(votoRepository.countByPautaIdAndOpcaoVoto(1L, Voto.OpcaoVoto.SIM)).thenReturn(7L);
        when(votoRepository.countByPautaIdAndOpcaoVoto(1L, Voto.OpcaoVoto.NAO)).thenReturn(3L);

        ResultadoVotacaoDTO result = sessaoVotacaoService.obterResultado(1L);

        assertNotNull(result);
        assertEquals(pauta.getId(), result.getPautaId());
        assertEquals(pauta.getTitulo(), result.getTituloPauta());
        assertEquals(10L, result.getTotalVotos());
        assertEquals(7L, result.getVotosSim());
        assertEquals(3L, result.getVotosNao());
        assertEquals("APROVADA", result.getResultado());
        assertFalse(result.isSessaoEncerrada());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar obter resultado de pauta inexistente")
    void obterResultadoPautaInexistente()
    {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
        {
            sessaoVotacaoService.obterResultado(99L);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar obter resultado de pauta sem sessão")
    void obterResultadoPautaSemSessao()
    {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
        {
            sessaoVotacaoService.obterResultado(1L);
        });
    }
} 