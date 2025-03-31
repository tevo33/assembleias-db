package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PautaDTO
{
    private Long id;
    
    @NotBlank( message = "O título da pauta é obrigatório" )
    private String titulo;
    
    private String descricao;
} 