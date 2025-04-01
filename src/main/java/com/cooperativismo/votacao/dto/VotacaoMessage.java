package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VotacaoMessage
{
    private Long sessaoId;
    private Long pautaId;
    private String cpfAssociado;
    private String voto;
    private Long timestamp;
} 