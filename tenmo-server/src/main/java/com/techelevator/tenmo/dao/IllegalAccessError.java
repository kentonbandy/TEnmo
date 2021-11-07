package com.techelevator.tenmo.dao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalAccessError extends Exception {

    public IllegalAccessError() {
        super ("This user does not have permission to do that.");
    }
}
