package com.cooperativismo.votacao.repository;

import com.cooperativismo.votacao.model.Voto;
import com.cooperativismo.votacao.model.Voto.OpcaoVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long>
{
    Optional<Voto> findByPautaIdAndCpfAssociado( Long pautaId, String cpfAssociado );
    
    long countByPautaId( Long pautaId );
    
    long countByPautaIdAndOpcaoVoto( Long pautaId, OpcaoVoto opcaoVoto );
} 