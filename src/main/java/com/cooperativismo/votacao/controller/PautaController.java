package com.cooperativismo.votacao.controller;

import com.cooperativismo.votacao.dto.PautaDTO;
import com.cooperativismo.votacao.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequestMapping( "/v1/pautas" )
@RequiredArgsConstructor
@Tag( name = "Pautas", description = "APIs para gerenciamento de pautas" )
public class PautaController
{
    private final PautaService pautaService;

    @GetMapping
    @Operation( summary = "Listar todas as pautas", description = "Retorna todas as pautas cadastradas no sistema" )
    @ApiResponses( value = {
        @ApiResponse( responseCode = "200", description = "Pautas listadas com sucesso" )
    } )

    public ResponseEntity<List<PautaDTO>> listarPautas()
    {
        log.info( "Recebida requisição para listar pautas" );
    
        return ResponseEntity.ok( pautaService.listarPautas() );
    }

    @GetMapping( "/{id}" )
    @Operation(summary = "Buscar pauta pelo ID",
               description = "Retorna uma pauta específica pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pauta encontrada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public ResponseEntity<PautaDTO> buscarPauta( @PathVariable Long id )
    {
        log.info( "Recebida requisição para buscar pauta ID: {}", id );
    
        return ResponseEntity.ok( pautaService.buscarPauta( id ) );
    }

    @PostMapping
    @Operation(summary = "Criar nova pauta",
               description = "Cria uma nova pauta no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pauta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida")
    } )
    public ResponseEntity<PautaDTO> criarPauta( @Valid @RequestBody PautaDTO pautaDTO ) 
    {
        log.info(  "Recebida requisição para criar pauta: {}", pautaDTO.getTitulo() );
    
        PautaDTO pautaCriada = pautaService.criarPauta( pautaDTO );
        
        return ResponseEntity.status( HttpStatus.CREATED ).body( pautaCriada );
    }
} 