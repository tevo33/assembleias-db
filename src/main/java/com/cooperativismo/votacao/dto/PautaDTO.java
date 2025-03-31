package com.cooperativismo.votacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

import com.cooperativismo.votacao.model.Pauta;

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

    public static PautaDTO convertToDto( Pauta pauta )
    {
        return PautaDTO.builder()
                       .id( pauta.getId() )
                       .titulo( pauta.getTitulo() )
                       .descricao( pauta.getDescricao() )
                       .build();
    }
} 