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
@Table(uniqueConstraints = 
{
    @UniqueConstraint( columnNames = { "pauta_id", "cpf_associado" }, name = "uk_associado_pauta" )
} )
public class Voto
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "pauta_id" )
    private Pauta pauta;

    @Column( name = "cpf_associado", nullable = false )
    private String cpfAssociado;

    @Enumerated( EnumType.STRING )
    @Column( name = "voto", nullable = false )
    private OpcaoVoto opcaoVoto;

    @Column( name = "data_voto" )
    private LocalDateTime dataVoto;

    @PrePersist
    public void prePersist() 
    {
        dataVoto = LocalDateTime.now();
    }

    public enum OpcaoVoto 
    {
        SIM, NAO
    }
} 