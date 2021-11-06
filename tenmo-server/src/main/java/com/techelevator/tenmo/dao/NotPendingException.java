package com.techelevator.tenmo.dao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotPendingException extends Exception {

    public NotPendingException() {
        super("That transaction is not in 'Pending' status!");
    }
}
