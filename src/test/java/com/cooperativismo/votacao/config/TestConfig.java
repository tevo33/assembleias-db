package com.cooperativismo.votacao.config;

import com.cooperativismo.votacao.client.CpfValidatorClient;
import com.cooperativismo.votacao.dto.ValidacaoCpfDTO;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile( "test" )
public class TestConfig 
{
    @Bean
    @Primary
    public CpfValidatorClient cpfValidatorClient()
    {
        CpfValidatorClient mockClient = Mockito.mock( CpfValidatorClient.class );
        
        Mockito.when( mockClient.validarCpf( Mockito.anyString() ) )
               .thenReturn( new ValidacaoCpfDTO( "ABLE_TO_VOTE" ) );
        
        return mockClient;
    }
} 