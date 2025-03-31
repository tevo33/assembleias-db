package com.cooperativismo.votacao.integration;

import com.cooperativismo.votacao.dto.PautaDTO;
import com.cooperativismo.votacao.dto.VotoDTO;
import com.cooperativismo.votacao.model.Voto.OpcaoVoto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
public class VotacaoFluxoCompletoIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName( "Deve executar fluxo completo de votação com sucesso" )
    void fluxoCompletoVotacao() throws Exception
    {
        PautaDTO novaPauta = PautaDTO.builder()
                                     .titulo( "Pauta de Teste - Fluxo Completo" )
                                     .descricao( "Descrição da pauta de teste para fluxo completo" )
                                     .build();

        ResultActions createPautaResponse = mockMvc.perform( post( "/v1/pautas" )
                                                   .contentType( MediaType.APPLICATION_JSON )
                                                   .content( objectMapper.writeValueAsString( novaPauta ) ) )
                                                   .andDo( MockMvcResultHandlers.print() );

        createPautaResponse.andExpect( status().isCreated() ).andExpect( jsonPath( "$.id", is( notNullValue() ) ) );

        MvcResult createPautaResult = createPautaResponse.andReturn();
        String contentAsString = createPautaResult.getResponse().getContentAsString();
        PautaDTO pautaCriada = objectMapper.readValue( contentAsString, PautaDTO.class );
        assertNotNull( pautaCriada.getId() );
        Long pautaId = pautaCriada.getId();

        ResultActions abrirSessaoResponse = mockMvc.perform( post( "/v1/sessoes" )
                                                   .param( "pautaId", pautaId.toString() )
                                                   .param( "duracaoMinutos", "5" )
                                                   .contentType( MediaType.APPLICATION_JSON ) )
                                                   .andDo( MockMvcResultHandlers.print() );

        abrirSessaoResponse.andExpect( status().isCreated() )
                           .andExpect( jsonPath( "$.pautaId", is( pautaId.intValue() ) ) )
                           .andExpect( jsonPath( "$.ativa", is( true ) ) );

        VotoDTO voto1 = VotoDTO.builder()
                               .pautaId( pautaId )
                               .cpfAssociado( "12345678901" )
                               .opcaoVoto( OpcaoVoto.SIM )
                               .build();

        mockMvc.perform( post( "/v1/votos" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsString( voto1 ) ) )
                        .andExpect( status().isCreated() )
                        .andExpect( jsonPath( "$.pautaId", is( pautaId.intValue() ) ) )
                        .andExpect( jsonPath( "$.cpfAssociado", is( voto1.getCpfAssociado() ) ) )
                        .andExpect( jsonPath( "$.opcaoVoto", is( voto1.getOpcaoVoto().toString() ) ) );

        VotoDTO voto2 = VotoDTO.builder()
                               .pautaId( pautaId )
                               .cpfAssociado( "12345678902" )
                               .opcaoVoto( OpcaoVoto.SIM )
                               .build();

        mockMvc.perform( post( "/v1/votos" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsString( voto2 ) ) )
                        .andExpect( status().isCreated() );

        VotoDTO voto3 = VotoDTO.builder()
                               .pautaId( pautaId )
                               .cpfAssociado( "12345678903" )
                               .opcaoVoto( OpcaoVoto.NAO )
                               .build();

        mockMvc.perform( post( "/v1/votos" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsString( voto3 ) ) )
                        .andExpect( status().isCreated() );

        ResultActions resultadoResponse = mockMvc.perform( get( "/v1/sessoes/{pautaId}/resultado", pautaId )
                                                 .contentType( MediaType.APPLICATION_JSON ) )
                                                 .andDo( MockMvcResultHandlers.print() );

        resultadoResponse.andExpect( status().isOk() )
                         .andExpect( jsonPath( "$.pautaId", is( pautaId.intValue() ) ) )
                         .andExpect( jsonPath( "$.totalVotos", is( 3 ) ) )
                         .andExpect( jsonPath( "$.votosSim", is( 2 ) ) )
                         .andExpect( jsonPath( "$.votosNao", is( 1 ) ) )
                         .andExpect( jsonPath( "$.resultado", is( "APROVADA" ) ) );
    }
} 