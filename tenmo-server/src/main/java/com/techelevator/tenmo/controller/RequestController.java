package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
public class RequestController {
    private UserDao dao;

    public RequestController(UserDao userDao) {
        this.dao = userDao;
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public double getBalance(Principal user) {
        return dao.getBalanceByUsername(user.getName());
    }
}
