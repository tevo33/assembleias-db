package com.cooperativismo.votacao.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse 
{
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
} 