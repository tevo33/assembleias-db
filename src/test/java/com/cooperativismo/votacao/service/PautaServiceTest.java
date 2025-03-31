package com.cooperativismo.votacao.service;

import com.cooperativismo.votacao.dto.PautaDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
class PautaServiceTest
{
    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    private Pauta pauta;
    private PautaDTO pautaDTO;

    @BeforeEach
    void setUp()
    {
        pauta = Pauta.builder()
                     .id( 1L )
                     .titulo( "Pauta de Teste" )
                     .descricao( "Descrição da pauta de teste" )
                     .dataCriacao( LocalDateTime.now() )
                     .build();

        pautaDTO = PautaDTO.builder()
                           .id( 1L )
                           .titulo( "Pauta de Teste" )
                           .descricao( "Descrição da pauta de teste" )
                           .build();
    }

    @Test
    @DisplayName( "Deve listar todas as pautas com sucesso" )
    void listarPautas()
    {
        when( pautaRepository.findAll() ).thenReturn( Arrays.asList( pauta ) );

        List<PautaDTO> result = pautaService.listarPautas();

        assertNotNull( result );
        assertFalse( result.isEmpty() );
        assertEquals( 1, result.size() );
        assertEquals( pauta.getId(), result.get( 0 ).getId() );
        assertEquals( pauta.getTitulo(), result.get( 0 ).getTitulo() );
        assertEquals( pauta.getDescricao(), result.get( 0 ).getDescricao() );

        verify( pautaRepository, times( 1 ) ).findAll();
    }

    @Test
    @DisplayName( "Deve buscar pauta por ID com sucesso" )
    void buscarPautaComSucesso()
    {
        when( pautaRepository.findById( 1L ) ).thenReturn( Optional.of( pauta ) );

        PautaDTO result = pautaService.buscarPauta( 1L );

        assertNotNull( result );
        assertEquals( pauta.getId(), result.getId() );
        assertEquals( pauta.getTitulo(), result.getTitulo() );
        assertEquals( pauta.getDescricao(), result.getDescricao() );

        verify( pautaRepository, times( 1 ) ).findById( 1L );
    }

    @Test
    @DisplayName( "Deve lançar exceção ao buscar pauta inexistente" )
    void buscarPautaInexistente()
    {
        when( pautaRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> 
        {
            pautaService.buscarPauta( 99L );
        } );

        verify( pautaRepository, times( 1 ) ).findById( 99L );
    }

    @Test
    @DisplayName( "Deve criar pauta com sucesso" )
    void criarPauta()
    {
        PautaDTO novaPauta = PautaDTO.builder()
                                     .titulo( "Nova Pauta" )
                                     .descricao( "Descrição da nova pauta" )
                                     .build();

        Pauta pautaCriada = Pauta.builder()
                                 .id( 1L )
                                 .titulo( "Nova Pauta" )
                                 .descricao( "Descrição da nova pauta" )
                                 .dataCriacao( LocalDateTime.now() )
                                 .build();

        when( pautaRepository.save( any( Pauta.class ) ) ).thenReturn( pautaCriada );

        PautaDTO result = pautaService.criarPauta( novaPauta );

        assertNotNull( result );
        assertEquals( pautaCriada.getId(), result.getId() );
        assertEquals( novaPauta.getTitulo(), result.getTitulo() );
        assertEquals( novaPauta.getDescricao(), result.getDescricao() );

        verify( pautaRepository, times( 1 ) ).save( any( Pauta.class ) );
    }
} 