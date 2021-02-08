package com.keta.rule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class ClusterException extends RuntimeException{

    public ClusterException(String message,Exception root){
        super(message,root);
    }

}
