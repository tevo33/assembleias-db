package com.cooperativismo.votacao.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler 
{
    @ExceptionHandler( ResourceNotFoundException.class )
    @ResponseStatus( HttpStatus.NOT_FOUND )
    public ErrorResponse handleResourceNotFoundException( ResourceNotFoundException ex, HttpServletRequest request )
    {
        return ErrorResponse.builder().timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.NOT_FOUND.value() )
                                      .error( "Recurso não encontrado" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
    }

    @ExceptionHandler( InvalidCpfException.class )
    @ResponseStatus( HttpStatus.NOT_FOUND )
    public ErrorResponse handleInvalidCpfException( InvalidCpfException ex, HttpServletRequest request )
    {
        return ErrorResponse.builder().timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.NOT_FOUND.value() )
                                      .error( "CPF inválido" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
    }

    @ExceptionHandler( UnableToVoteException.class )
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ErrorResponse handleUnableToVoteException( UnableToVoteException ex, HttpServletRequest request)
    {
        return ErrorResponse.builder().timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.BAD_REQUEST.value() )
                                      .error( "Não autorizado para votar" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
    }

    @ExceptionHandler( BusinessException.class )
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ErrorResponse handleBusinessException( BusinessException ex, HttpServletRequest request )
    {
        return ErrorResponse.builder().timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.BAD_REQUEST.value() )
                                      .error( "Erro de negócio" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
    }

    @ExceptionHandler( MethodArgumentNotValidException.class )
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ResponseEntity<Map<String, String>> handleValidationExceptions( MethodArgumentNotValidException ex )
    {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach( ( error ) -> 
        {
            String fieldName = ( (FieldError) error ).getField();
            String errorMessage = error.getDefaultMessage();
            
            errors.put( fieldName, errorMessage );
        } );
        
        return ResponseEntity.badRequest().body( errors );
    }

    @ExceptionHandler( Exception.class )
    @ResponseStatus( HttpStatus.INTERNAL_SERVER_ERROR )
    public ErrorResponse handleGlobalException( Exception ex, HttpServletRequest request ) 
    {
        return ErrorResponse.builder().timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.INTERNAL_SERVER_ERROR.value() )
                                      .error( "Erro no servidor" )
                                      .message( "Ocorreu um erro interno no servidor" )
                                      .path( request.getRequestURI() )
                                      .build();
    }

    @Builder
    @AllArgsConstructor
    public static class ErrorResponse 
    {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
} 