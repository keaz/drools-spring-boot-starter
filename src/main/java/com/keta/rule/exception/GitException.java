package com.keta.rule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class GitException extends RuntimeException {

    public GitException(String message,Exception e) {
        super(message,e);
    }
}
