package com.cooperativismo.votacao.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping( "/api/callbacks" )
public class CallbackTestController
{
    @PostMapping( "/sessao-encerrada" )
    public ResponseEntity<String> receberNotificacaoSessaoEncerrada( @RequestBody Map<String, Object> payload )
    {
        log.info( "Callback recebido - Sessão Encerrada: {}", payload );
    
        return ResponseEntity.ok( "Callback de sessão encerrada recebido com sucesso" );
    }

    @PostMapping( "/resultado-votacao" )
    public ResponseEntity<String> receberNotificacaoResultadoVotacao( @RequestBody Map<String, Object> payload )
    {
        log.info( "Callback recebido - Resultado Votação: {}", payload );
        
        return ResponseEntity.ok( "Callback de resultado de votação recebido com sucesso" );
    }
} 