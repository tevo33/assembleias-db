package com.cooperativismo.votacao.controller;

import com.cooperativismo.votacao.dto.VotoDTO;
import com.cooperativismo.votacao.exception.ResourceNotFoundException;
import com.cooperativismo.votacao.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping( "/v1/votos" )
@RequiredArgsConstructor
@Tag( name = "Votos", description = "APIs para gerenciamento de votos" )
public class VotoController
{
    private final VotoService votoService;

    @PostMapping
    @Operation( summary = "Registrar voto",
               description = "Solicita o registro assíncrono do voto de um associado em uma pauta específica" )
    @ApiResponses( value = {
        @ApiResponse( responseCode = "201", description = "Voto registrado com sucesso" ),
        @ApiResponse( responseCode = "400", description = "Requisição inválida ou CPF inválido" ),
        @ApiResponse( responseCode = "404", description = "Pauta não encontrada" )
    } )
    public CompletableFuture<ResponseEntity<VotoDTO>> registrarVoto( @Valid @RequestBody VotoDTO votoDTO )
    {
        log.info( "Recebida requisição para registrar voto do associado {} na pauta {}", votoDTO.getCpfAssociado(), votoDTO.getPautaId() );
        
        return CompletableFuture.supplyAsync( () ->
        {
            try
            {
                votoService.verificarPautaExiste( votoDTO.getPautaId() );
                votoService.registrarVoto( votoDTO );
            
                return ResponseEntity.status( HttpStatus.CREATED ).body( votoDTO );
            }
            
            catch ( ResourceNotFoundException e )
            {
                throw e;
            }
        } );
    }
} 