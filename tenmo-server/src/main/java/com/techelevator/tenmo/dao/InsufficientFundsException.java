package com.techelevator.tenmo.dao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException() {
        super("The sender has insufficient funds for this transaction.");
    }
}
