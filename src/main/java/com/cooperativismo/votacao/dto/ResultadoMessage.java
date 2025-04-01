package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoMessage
{
    private Long pautaId;
    private String tituloPauta;
    private Long totalVotos;
    private Long votosSim;
    private Long votosNao;
    private String resultado;
    private Long timestamp;
} 