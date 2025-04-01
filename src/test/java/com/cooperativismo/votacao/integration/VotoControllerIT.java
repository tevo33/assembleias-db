package com.cooperativismo.votacao.integration;

import com.cooperativismo.votacao.dto.VotoDTO;
import com.cooperativismo.votacao.model.Pauta;
import com.cooperativismo.votacao.model.SessaoVotacao;
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
public class VotoControllerIT
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
    private SessaoVotacao sessaoVotacao;

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

        sessaoVotacao = SessaoVotacao.builder()
                                     .pauta( pauta )
                                     .dataAbertura( LocalDateTime.now() )
                                     .dataFechamento( LocalDateTime.now().plusMinutes( 30 ) )
                                     .ativa( true )
                                     .build();

        sessaoVotacao = sessaoVotacaoRepository.save( sessaoVotacao );
    }

    @AfterEach
    void tearDown()
    {
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    @DisplayName( "Deve aceitar solicitação assíncrona de registro de voto" )
    void registrarVotoComSucesso() throws Exception
    {
        VotoDTO votoDTO = VotoDTO.builder()
                                 .pautaId( pauta.getId() )
                                 .cpfAssociado( "12345678901" )
                                 .opcaoVoto( OpcaoVoto.SIM )
                                 .build();

        ResultActions response = mockMvc.perform( post( "/v1/votos" )
                                        .contentType( MediaType.APPLICATION_JSON )
                                        .content( objectMapper.writeValueAsString( votoDTO ) ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isCreated() );
    }

    @Test
    @DisplayName( "Deve retornar 400 ao tentar votar com CPF inválido" )
    void registrarVotoCpfInvalido() throws Exception
    {
        VotoDTO votoDTO = VotoDTO.builder()
                                 .pautaId( pauta.getId() )
                                 .cpfAssociado( "123456" )
                                 .opcaoVoto( OpcaoVoto.SIM )
                                 .build();

        ResultActions response = mockMvc.perform( post( "/v1/votos" )
                                        .contentType( MediaType.APPLICATION_JSON )
                                        .content( objectMapper.writeValueAsString( votoDTO ) ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isBadRequest() );
    }

    @Test
    @DisplayName( "Deve retornar 400 ao tentar votar em uma pauta sem voto" )
    void registrarVotoSemOpcao() throws Exception
    {
        VotoDTO votoDTO = VotoDTO.builder()
                                 .pautaId( pauta.getId() )
                                 .cpfAssociado( "12345678901" )
                                 .opcaoVoto( null )
                                 .build();

        ResultActions response = mockMvc.perform( post( "/v1/votos" )
                                        .contentType( MediaType.APPLICATION_JSON )
                                        .content( objectMapper.writeValueAsString( votoDTO ) ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isBadRequest() );
    }

    @Test
    @DisplayName( "Deve retornar 404 ao tentar votar em uma pauta inexistente" )
    void registrarVotoPautaInexistente() throws Exception
    {
        VotoDTO votoDTO = VotoDTO.builder()
                                 .pautaId( 99999L )
                                 .cpfAssociado( "12345678901" )
                                 .opcaoVoto( OpcaoVoto.SIM )
                                 .build();
                                 
        ResultActions response = mockMvc.perform( post( "/v1/votos" )
                                        .contentType( MediaType.APPLICATION_JSON )
                                        .content( objectMapper.writeValueAsString( votoDTO ) ) )
                                        .andDo( MockMvcResultHandlers.print() );

        response.andExpect( status().isNotFound() );
    }
} 