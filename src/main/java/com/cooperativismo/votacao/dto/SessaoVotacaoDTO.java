package com.cooperativismo.votacao.dto;

import com.cooperativismo.votacao.model.SessaoVotacao;
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
    public static final double DURACAO_MINIMA = 60.0;

    private Long id;
    
    @NotNull( message = "O ID da pauta é obrigatório" )
    private Long pautaId;
    
    @Min( value = 1, message = "A duração da sessão deve ser de pelo menos 1 minuto" )
    private Integer duracaoMinutos;
    
    private LocalDateTime dataAbertura;
    
    private LocalDateTime dataFechamento;
    
    private boolean ativa;

    public static SessaoVotacaoDTO convertToDto( SessaoVotacao sessaoVotacao )
    {
        int duracaoMinutos = getDuracaoEmMinutos( sessaoVotacao.getDataAbertura(), sessaoVotacao.getDataFechamento() );
        
        return SessaoVotacaoDTO.builder()
                               .id( sessaoVotacao.getId() )
                               .duracaoMinutos( duracaoMinutos )
                               .pautaId( sessaoVotacao.getPauta().getId() )
                               .dataAbertura( sessaoVotacao.getDataAbertura() )
                               .dataFechamento( sessaoVotacao.getDataFechamento() )
                               .ativa( sessaoVotacao.isAtiva() )
                               .build();
    }

    public SessaoVotacao convertToEntity()
    {
        LocalDateTime dataAbertura = LocalDateTime.now();
        LocalDateTime dataFechamento = dataAbertura.plusMinutes( this.duracaoMinutos );
        
        return SessaoVotacao.builder()
                           .dataAbertura( dataAbertura )
                           .dataFechamento( dataFechamento )
                           .ativa( true )
                           .build();
    }

    private static int getDuracaoEmMinutos( LocalDateTime dataAbertura, LocalDateTime dataFechamento )
    {
        long duracaoSegundos = java.time.Duration.between( dataAbertura, dataFechamento ).getSeconds();
        
        return Double.valueOf( Math.max( 1, Math.ceil( duracaoSegundos / DURACAO_MINIMA ) ) ).intValue();
    }
}