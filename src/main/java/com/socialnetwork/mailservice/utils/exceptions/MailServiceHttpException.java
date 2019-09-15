package com.socialnetwork.mailservice.utils.exceptions;

import org.springframework.http.HttpStatus;

public class MailServiceHttpException extends RuntimeException {
    private HttpStatus httpStatus;

    public MailServiceHttpException(String message,
                                    HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
