package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoVotacaoDTO
{
    private Long id;
    
    @NotNull( message = "O ID da pauta é obrigatório" )
    private Long pautaId;
    
    @Min( value = 1, message = "A duração da sessão deve ser de pelo menos 1 minuto" )
    private Integer duracaoMinutos;
    
    private LocalDateTime dataAbertura;
    
    private LocalDateTime dataFechamento;
    
    private boolean ativa;
} 