package com.cooperativismo.votacao.dto;

import com.cooperativismo.votacao.model.Voto;
import com.cooperativismo.votacao.model.Voto.OpcaoVoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotoDTO
{
    private Long id;
    
    @NotNull( message = "O ID da pauta é obrigatório" )
    private Long pautaId;
    
    @NotBlank( message = "O CPF do associado é obrigatório" )
    @Pattern( regexp = "^\\d{11}$", message = "CPF inválido, deve conter 11 dígitos" )
    private String cpfAssociado;
    
    @NotNull( message = "O voto é obrigatório (SIM ou NAO)" )
    private OpcaoVoto opcaoVoto;

    public static VotoDTO convertToDto( Voto voto )
    {
        return VotoDTO.builder()
                      .id( voto.getId() )
                      .pautaId( voto.getPauta().getId() )
                      .cpfAssociado( voto.getCpfAssociado() )
                      .opcaoVoto( voto.getOpcaoVoto() )
                      .build();
    }

    public Voto convertToEntity()
    {
        return Voto.builder()
                   .cpfAssociado( this.cpfAssociado )
                   .opcaoVoto( this.opcaoVoto )
                   .build();
    }
} 