package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
public class TenmoController {
    private UserDao dao;

    public TenmoController(UserDao userDao) {
        this.dao = userDao;
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public double getBalance(Principal user) {
        return dao.getBalanceByUsername(user.getName());
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public Transfer processTransfer(@RequestBody Transfer transfer) {
        return dao.createTransfer(transfer);
    }

}
