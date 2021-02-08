package com.keta.rule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NotifyException extends ClusterException{

    public NotifyException(String message,Exception root){
        super(message,root);
    }

}
