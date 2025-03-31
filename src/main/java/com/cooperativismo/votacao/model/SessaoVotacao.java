package com.cooperativismo.votacao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoVotacao
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @OneToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "pauta_id", unique = true )
    private Pauta pauta;

    @Column( name = "data_abertura" )
    private LocalDateTime dataAbertura;

    @Column( name = "data_fechamento" )
    private LocalDateTime dataFechamento;

    @Column( name = "ativa" )
    private boolean ativa;

    @PrePersist
    public void prePersist()
    {
        dataAbertura = LocalDateTime.now();
        ativa = true;
    }

    public boolean estaAberta()
    {
        return ativa && LocalDateTime.now().isBefore( dataFechamento );
    }

    public void fechar() 
    {
        ativa = false;
    }
} 