package com.cooperativismo.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig 
{
    @Value( "${server.servlet.context-path}" )
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() 
    {
        return new OpenAPI().servers( List.of( new Server().url( contextPath ) ) )
                            .info( new Info().title( "API de Votação" )
                                             .description( "API REST para votações em cooperativas" )
                                             .version( "1.0.0" )
                            .contact( new Contact().name( "dev" )
                                                   .email( "tevoguerra@gmail.com" ) )
                            .license( new License().name( "Apache 2.0" )
                                                   .url( "http://www.apache.org/licenses/LICENSE-2.0.html" ) ) );
    }
} 