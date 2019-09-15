package com.socialnetwork.mailservice.controllers;


import com.socialnetwork.mailservice.utils.exceptions.MailServiceHttpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ExceptionHandlingController {

    @ExceptionHandler
    public ResponseEntity<String> exceptionHandler(Exception ex) {
        if (ex instanceof MailServiceHttpException) {
            return new ResponseEntity<>(ex.getMessage(), ((MailServiceHttpException) ex).getHttpStatus());
        } else if (ex instanceof MethodArgumentNotValidException) {
            return new ResponseEntity<>(ex.getMessage(), BAD_REQUEST);
        } else {
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

}
