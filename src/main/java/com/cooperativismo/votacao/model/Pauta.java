package com.cooperativismo.votacao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pauta
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @NotBlank( message = "O título da pauta é obrigatório" )
    private String titulo;

    @Column( length = 1000 )
    private String descricao;

    @Column( name = "data_criacao" )
    private LocalDateTime dataCriacao;

    @OneToOne( mappedBy = "pauta", cascade = CascadeType.ALL, fetch = FetchType.LAZY )
    private SessaoVotacao sessaoVotacao;

    @OneToMany( mappedBy = "pauta", cascade = CascadeType.ALL, fetch = FetchType.LAZY )
    private List<Voto> votos = new ArrayList<>();

    @PrePersist
    public void prePersist()
    {
        dataCriacao = LocalDateTime.now();
    }
} 