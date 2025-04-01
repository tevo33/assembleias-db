package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PautaMessage
{
    private Long pautaId;
    private String titulo;
    private String descricao;
    private String tipoOperacao;
    private Long timestamp;
} 