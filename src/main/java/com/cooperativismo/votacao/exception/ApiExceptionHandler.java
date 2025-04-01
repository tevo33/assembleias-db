package com.cooperativismo.votacao.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler 
{
    @ExceptionHandler( ResourceNotFoundException.class )
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException( ResourceNotFoundException ex, HttpServletRequest request )
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                      .timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.NOT_FOUND.value() )
                                      .error( "Recurso não encontrado" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
        
        return ResponseEntity.status( HttpStatus.NOT_FOUND )
                             .contentType( MediaType.APPLICATION_JSON )
                             .body( errorResponse );
    }

    @ExceptionHandler( InvalidCpfException.class )
    public ResponseEntity<ErrorResponse> handleInvalidCpfException( InvalidCpfException ex, HttpServletRequest request )
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                      .timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.BAD_REQUEST.value() )
                                      .error("CPF inválido" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
        
        return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                             .contentType( MediaType.APPLICATION_JSON )
                             .body( errorResponse );
    }

    @ExceptionHandler( UnableToVoteException.class )
    public ResponseEntity<ErrorResponse> handleUnableToVoteException( UnableToVoteException ex, HttpServletRequest request)
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                      .timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.BAD_REQUEST.value() )
                                      .error( "Não autorizado para votar" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
        
        return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                             .contentType( MediaType.APPLICATION_JSON )
                             .body( errorResponse );
    }

    @ExceptionHandler( BusinessException.class )
    public ResponseEntity<ErrorResponse> handleBusinessException( BusinessException ex, HttpServletRequest request )
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                      .timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.BAD_REQUEST.value() )
                                      .error( "Erro de negócio" )
                                      .message( ex.getMessage() )
                                      .path( request.getRequestURI() )
                                      .build();
        
        return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                             .contentType( MediaType.APPLICATION_JSON )
                             .body( errorResponse );
    }

    @ExceptionHandler( MethodArgumentNotValidException.class )
    public ResponseEntity<Map<String, String>> handleValidationExceptions( MethodArgumentNotValidException ex )
    {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach( ( error ) -> 
        {
            String fieldName = ( (FieldError) error ).getField();
            String errorMessage = error.getDefaultMessage();
            
            errors.put( fieldName, errorMessage );
        } );
        
        return ResponseEntity.badRequest()
                             .contentType( MediaType.APPLICATION_JSON )
                             .body( errors );
    }

    @ExceptionHandler( Exception.class )
    public ResponseEntity<ErrorResponse> handleGlobalException( Exception ex, HttpServletRequest request ) 
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                      .timestamp( LocalDateTime.now() )
                                      .status( HttpStatus.INTERNAL_SERVER_ERROR.value() )
                                      .error( "Erro no servidor" )
                                      .message( "Ocorreu um erro interno no servidor" )
                                      .path( request.getRequestURI() )
                                      .build();
        
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                             .contentType( MediaType.APPLICATION_JSON )
                             .body( errorResponse );
    }
} 