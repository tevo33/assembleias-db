package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoVotacaoDTO 
{
    private Long pautaId;
    private String tituloPauta;
    private Long totalVotos;
    private Long votosSim;
    private Long votosNao;
    private String resultado;
    private boolean sessaoEncerrada;
} 