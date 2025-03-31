package com.cooperativismo.votacao.exception;

public class ResourceNotFoundException extends RuntimeException
{
    public ResourceNotFoundException( String message )
    {
        super( message );
    }

    public ResourceNotFoundException( String resource, Long id )
    {
        super( String.format(  "%s não encontrado(a) com o ID: %d", resource, id ) );
    }
} 