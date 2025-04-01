package com.cooperativismo.votacao.integration;

import com.cooperativismo.votacao.dto.PautaDTO;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.repository.PautaRepository;
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
public class PautaControllerIT
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pauta pautaExistente;

    @BeforeEach
    void setUp()
    {
        pautaRepository.deleteAll();

        pautaExistente = Pauta.builder()
                              .titulo( "Pauta de Teste" )
                              .descricao( "Descrição da pauta de teste" )
                              .dataCriacao( LocalDateTime.now() )
                              .build();

        pautaExistente = pautaRepository.save( pautaExistente );
    }

    @AfterEach
    void tearDown()
    {
        pautaRepository.deleteAll();
    }

    @Test
    @DisplayName( "Deve listar todas as pautas com sucesso" )
    void listarPautasComSucesso() throws Exception
    {
        ResultActions response = mockMvc.perform( get( "/v1/pautas" )
                                        .contentType( MediaType.APPLICATION_JSON ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isOk() )
                .andExpect( jsonPath( "$", hasSize( greaterThanOrEqualTo( 1 ) ) ) )
                .andExpect( jsonPath( "$[0].id", is( pautaExistente.getId().intValue() ) ) )
                .andExpect( jsonPath( "$[0].titulo", is( pautaExistente.getTitulo() ) ) )
                .andExpect( jsonPath( "$[0].descricao", is( pautaExistente.getDescricao() ) ) );
    }

    @Test
    @DisplayName( "Deve buscar pauta por ID com sucesso" )
    void buscarPautaComSucesso() throws Exception
    {
        ResultActions response = mockMvc.perform( get( "/v1/pautas/{id}", pautaExistente.getId() )
                                        .contentType( MediaType.APPLICATION_JSON ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", is( pautaExistente.getId().intValue() ) ) )
                .andExpect( jsonPath( "$.titulo", is( pautaExistente.getTitulo() ) ) )
                .andExpect( jsonPath( "$.descricao", is( pautaExistente.getDescricao() ) ) );
    }

    @Test
    @DisplayName( "Deve retornar 404 ao buscar pauta inexistente" )
    void buscarPautaInexistente() throws Exception
    {
        ResultActions response = mockMvc.perform( get( "/v1/pautas/{id}", 999L )
                                        .contentType( MediaType.APPLICATION_JSON ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName( "Deve aceitar solicitação assíncrona de criação de pauta" )
    void criarPautaComSucesso() throws Exception
    {
        PautaDTO novaPauta = PautaDTO.builder()
                                     .titulo( "Nova Pauta de Teste" )
                                     .descricao( "Descrição da nova pauta de teste" )
                                     .build();

        ResultActions response = mockMvc.perform( post( "/v1/pautas" )
                                        .contentType( MediaType.APPLICATION_JSON )
                                        .content( objectMapper.writeValueAsString( novaPauta ) ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isCreated() );
    }

    @Test
    @DisplayName( "Deve retornar 400 ao tentar criar pauta sem título" )
    void criarPautaSemTitulo() throws Exception
    {
        PautaDTO novaPauta = PautaDTO.builder()
                                     .descricao( "Descrição da nova pauta de teste" )
                                     .build();

        ResultActions response = mockMvc.perform( post( "/v1/pautas" )
                                        .contentType( MediaType.APPLICATION_JSON )
                                        .content( objectMapper.writeValueAsString( novaPauta ) ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isBadRequest() );
    }
} 