package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.PautaDTO;
import com.cooperativismo.votacao.dto.PautaMessage;
import com.cooperativismo.votacao.exception.ResourceNotFoundException;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest
{
    @Mock
    private PautaRepository pautaRepository;
    
    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private PautaService pautaService;

    private Pauta pauta;
    private PautaDTO pautaDTO;
    private PautaMessage pautaMessage;

    @BeforeEach
    void setUp()
    {
        pauta = Pauta.builder()
                     .id(1L)
                     .titulo("Pauta de Teste")
                     .descricao("Descrição da pauta de teste")
                     .dataCriacao(LocalDateTime.now())
                     .build();

        pautaDTO = PautaDTO.builder()
                           .id(1L)
                           .titulo("Pauta de Teste")
                           .descricao("Descrição da pauta de teste")
                           .build();
                           
        pautaMessage = new PautaMessage(
                        1L,
                        "Pauta de Teste",
                        "Descrição da pauta de teste",
                        "CREATE",
                        System.currentTimeMillis()
                      );
    }

    @Test
    @DisplayName("Deve listar todas as pautas com sucesso")
    void listarPautas()
    {
        when(pautaRepository.findAll()).thenReturn(Arrays.asList(pauta));

        List<PautaDTO> result = pautaService.listarPautas();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(pauta.getId(), result.get(0).getId());
        assertEquals(pauta.getTitulo(), result.get(0).getTitulo());
        assertEquals(pauta.getDescricao(), result.get(0).getDescricao());

        verify(pautaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar pauta por ID com sucesso")
    void buscarPautaComSucesso()
    {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        PautaDTO result = pautaService.buscarPauta(1L);

        assertNotNull(result);
        assertEquals(pauta.getId(), result.getId());
        assertEquals(pauta.getTitulo(), result.getTitulo());
        assertEquals(pauta.getDescricao(), result.getDescricao());

        verify(pautaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar pauta inexistente")
    void buscarPautaInexistente()
    {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
        {
            pautaService.buscarPauta(99L);
        } );

        verify( pautaRepository, times(1) ).findById( 99L );
    }

    @Test
    @DisplayName("Deve enviar mensagem para criar pauta")
    void criarPauta()
    {
        PautaDTO novaPauta = PautaDTO.builder()
                                     .titulo("Nova Pauta")
                                     .descricao("Descrição da nova pauta")
                                     .build();

        doNothing().when(kafkaService).sendMessage(eq("pauta-topic"), any(PautaMessage.class));
        
        pautaService.criarPauta(novaPauta);
        
        verify(kafkaService, times(1)).sendMessage(eq("pauta-topic"), any(PautaMessage.class));
    }
    
    @Test
    @DisplayName("Deve processar mensagem de criação de pauta")
    void processarPautaCreate()
    {
        PautaMessage message = new PautaMessage(
                                null,
                                "Nova Pauta",
                                "Descrição da nova pauta",
                                "CREATE",
                                System.currentTimeMillis()
                             );
        
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);
        
        pautaService.processarPauta(message);
        
        verify(pautaRepository, times(1)).save(any(Pauta.class));
    }
    
    @Test
    @DisplayName("Deve processar mensagem de atualização de pauta")
    void processarPautaUpdate()
    {
        PautaMessage message = new PautaMessage(
                                1L,
                                "Pauta Atualizada",
                                "Descrição atualizada",
                                "UPDATE",
                                System.currentTimeMillis()
                             );
        
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);
        
        pautaService.processarPauta(message);
        
        verify(pautaRepository, times(1)).findById(1L);
        verify(pautaRepository, times(1)).save(any(Pauta.class));
    }
    
    @Test
    @DisplayName("Deve processar mensagem de exclusão de pauta")
    void processarPautaDelete()
    {
        PautaMessage message = new PautaMessage(
                                1L,
                                "Pauta a ser excluída",
                                "Descrição da pauta",
                                "DELETE",
                                System.currentTimeMillis()
                             );
        
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        doNothing().when(pautaRepository).delete(pauta);
        
        pautaService.processarPauta(message);
        
        verify(pautaRepository, times(1)).findById(1L);
        verify(pautaRepository, times(1)).delete(pauta);
    }
} 