package com.techelevator.tenmo.dao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSuchTransactionIdException extends Exception {

    public NoSuchTransactionIdException() {
        super("That transaction ID doesn't exist!");
    }
}
