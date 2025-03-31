package com.cooperativismo.votacao.client;

import com.cooperativismo.votacao.dto.ValidacaoCpfDTO;
import com.cooperativismo.votacao.exception.InvalidCpfException;
import com.cooperativismo.votacao.exception.UnableToVoteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class CpfValidatorClient 
{
    private final Random random = new Random();

    public ValidacaoCpfDTO validarCpf( String cpf ) 
    {
        if ( cpf == null || cpf.length() != 11 || ! cpf.matches( "\\d+" ) )
        {
            log.info( "CPF com formato inválido: {}", cpf );
            
            throw new InvalidCpfException("CPF deve conter 11 dígitos numéricos" );
        }
        
        if ( random.nextInt( 10 ) < 3 ) 
        {
            log.info( "CPF simulado como inválido: {}", cpf );

            throw new InvalidCpfException( "CPF inválido" );
        }
        
        boolean podeVotar = random.nextBoolean();
        String status = podeVotar ? "ABLE_TO_VOTE" : "UNABLE_TO_VOTE";
        
        log.info( "Validação de CPF: {} - Status: {}", cpf, status );
        
        ValidacaoCpfDTO validacaoDTO = new ValidacaoCpfDTO( status );
        
        if ( ! podeVotar ) 
        {
            throw new UnableToVoteException( "Associado não está habilitado para votar" );
        }
        
        return validacaoDTO;
    }
}