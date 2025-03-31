package com.cooperativismo.votacao.controller;

import com.cooperativismo.votacao.dto.ResultadoVotacaoDTO;
import com.cooperativismo.votacao.dto.SessaoVotacaoDTO;
import com.cooperativismo.votacao.service.SessaoVotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping( "/v1/sessoes" )
@RequiredArgsConstructor
@Tag( name = "Sessões de Votação", description = "APIs para gerenciamento de sessões de votação" )
public class SessaoVotacaoController
{
    private final SessaoVotacaoService sessaoVotacaoService;

    @PostMapping
    @Operation( summary = "Abrir sessão de votação",
               description = "Abre uma sessão de votação para uma pauta específica com duração opcional em minutos (padrão: 1 minuto)")
    @ApiResponses( value = {
        @ApiResponse(responseCode = "201", description = "Sessão aberta com sucesso" ),
        @ApiResponse(responseCode = "400", description = "Requisição inválida ou pauta já possui sessão aberta" ),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada" )
    })
    public ResponseEntity<SessaoVotacaoDTO> abrirSessao( @RequestParam Long pautaId, @RequestParam( required = false ) Integer duracaoMinutos ) 
    {
        log.info( "Recebida requisição para abrir sessão de votação para pauta ID: {} com duração: {} minutos", pautaId, duracaoMinutos );
        
        SessaoVotacaoDTO sessao = sessaoVotacaoService.abrirSessao( pautaId, duracaoMinutos );
        
        return ResponseEntity.status( HttpStatus.CREATED ).body( sessao );
    }

    @GetMapping( "/{pautaId}/resultado" )
    @Operation(summary = "Obter resultado da votação",
               description = "Retorna o resultado da votação de uma pauta específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultado obtido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Pauta não possui sessão de votação"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    } )
    public ResponseEntity<ResultadoVotacaoDTO> obterResultado( @PathVariable Long pautaId )
    {
        log.info( "Recebida requisição para obter resultado da votação da pauta ID: {}", pautaId );
    
        return ResponseEntity.ok( sessaoVotacaoService.obterResultado( pautaId ) );
    }
} 