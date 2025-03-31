package com.cooperativismo.votacao.exception;

public class UnableToVoteException extends RuntimeException
{
    public UnableToVoteException( String message )
    {
        super( message );
    }
} 