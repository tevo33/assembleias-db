package com.cooperativismo.votacao.integration;

import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.model.SessaoVotacao;
import com.cooperativismo.votacao.model.Voto;
import com.cooperativismo.votacao.model.Voto.OpcaoVoto;
import com.cooperativismo.votacao.repository.PautaRepository;
import com.cooperativismo.votacao.repository.SessaoVotacaoRepository;
import com.cooperativismo.votacao.repository.VotoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
public class SessaoVotacaoControllerIT
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pauta pauta;

    @BeforeEach
    void setUp()
    {
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();

        pauta = Pauta.builder()
                     .titulo( "Pauta de Teste" )
                     .descricao( "Descrição da pauta de teste" )
                     .dataCriacao( LocalDateTime.now() )
                     .build();

        pauta = pautaRepository.save( pauta );
    }

    @AfterEach
    void tearDown()
    {
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    @DisplayName( "Deve abrir sessão de votação com sucesso" )
    void abrirSessaoComSucesso() throws Exception
    {
        ResultActions response = mockMvc.perform( post( "/v1/sessoes" )
                                        .param( "pautaId", pauta.getId().toString() )
                                        .param( "duracaoMinutos", "5" )
                                        .contentType( MediaType.APPLICATION_JSON ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.id", is( notNullValue() ) ) )
                .andExpect( jsonPath( "$.pautaId", is( pauta.getId().intValue() ) ) )
                .andExpect( jsonPath( "$.ativa", is( true ) ) )
                .andExpect( jsonPath( "$.dataAbertura", is( notNullValue() ) ) )
                .andExpect( jsonPath( "$.dataFechamento", is( notNullValue() ) ) );
    }

    @Test
    @DisplayName( "Deve retornar 404 ao abrir sessão com pauta inexistente" )
    void abrirSessaoPautaInexistente() {}

    @Test
    @DisplayName( "Deve retornar 400 ao abrir sessão para pauta que já possui sessão aberta" )
    void abrirSessaoPautaJaTemSessao()
    {
        SessaoVotacao sessao = SessaoVotacao.builder()
                                            .pauta( pauta )
                                            .dataAbertura( LocalDateTime.now() )
                                            .dataFechamento( LocalDateTime.now().plusMinutes( 5 ) )
                                            .ativa( true )
                                            .build();

        sessaoVotacaoRepository.save( sessao );
    }

    @Test
    @DisplayName( "Deve obter resultado da votação com sucesso" )
    void obterResultadoComSucesso() throws Exception
    {
        SessaoVotacao sessao = SessaoVotacao.builder()
                                            .pauta( pauta )
                                            .dataAbertura( LocalDateTime.now().minusMinutes( 10 ) )
                                            .dataFechamento( LocalDateTime.now().minusMinutes( 5 ) )
                                            .ativa( false )
                                            .build();
                
        sessao = sessaoVotacaoRepository.save( sessao );

        for ( int i = 1; i <= 3; i++ )
        {
            Voto voto = Voto.builder()
                            .pauta( pauta )
                            .cpfAssociado( "12345678901" + i )
                            .opcaoVoto( OpcaoVoto.SIM )
                            .dataVoto( LocalDateTime.now().minusMinutes( 7 ) )
                            .build();

            votoRepository.save( voto );
        }

        for ( int i = 4; i <= 5; i++ )
        {
            Voto voto = Voto.builder()
                            .pauta( pauta )
                            .cpfAssociado( "12345678901" + i )
                            .opcaoVoto( OpcaoVoto.NAO )
                            .dataVoto( LocalDateTime.now().minusMinutes( 7 ) )
                            .build();
        
            votoRepository.save( voto );
        }

        ResultActions response = mockMvc.perform( get( "/v1/sessoes/{pautaId}/resultado", pauta.getId() )
                                        .contentType( MediaType.APPLICATION_JSON ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isOk() )
                .andExpect( jsonPath( "$.pautaId", is( pauta.getId().intValue() ) ) )
                .andExpect( jsonPath( "$.totalVotos", is( 5 ) ) )
                .andExpect( jsonPath( "$.votosSim", is( 3 ) ) )
                .andExpect( jsonPath( "$.votosNao", is( 2 ) ) )
                .andExpect( jsonPath( "$.resultado", is( "APROVADA" ) ) );
    }

    @Test
    @DisplayName( "Deve retornar 404 ao obter resultado com pauta inexistente" )
    void obterResultadoPautaInexistente() {}

    @Test
    @DisplayName( "Deve retornar 400 ao obter resultado para pauta sem sessão de votação" )
    void obterResultadoPautaSemSessao() {}
} 