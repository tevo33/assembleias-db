package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoMessage
{
    private Long pautaId;
    private Long sessaoId;
    private String tipoNotificacao;
    private Object conteudo;
    private Long timestamp;
} 