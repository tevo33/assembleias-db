package com.cooperativismo.votacao.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;

@Getter
@ConstructorBinding
@ConfigurationProperties( prefix = "callback" )
public class CallbackConfig
{
    private final boolean enabled;
    private final String domain;
    private final Map<String, String> endpoints;

    public CallbackConfig( boolean enabled, String domain, Map<String, String> endpoints )
    {
        this.enabled = enabled;
        this.domain = domain;
        this.endpoints = endpoints;
    }

    public String getFullUrl( String endpointKey )
    {
        if ( ! enabled || ! endpoints.containsKey( endpointKey ) )
        {
            return null;
        }

        return domain + endpoints.get( endpointKey );
    }
} 