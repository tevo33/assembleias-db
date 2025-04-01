package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessaoMessage
{
    private Long sessaoId;
    private Long pautaId;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String tipoOperacao;
    private Long timestamp;
} 