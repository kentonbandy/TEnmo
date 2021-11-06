package com.techelevator.tenmo.dao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSuchTransferIdException extends Exception {

    public NoSuchTransferIdException() {
        super("That transfer ID doesn't exist!");
    }
}
