package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.InsufficientFundsException;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

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

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getUsers() {
        return dao.getAllUsers();
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> getTransfersForUser(Principal user) {
        return dao.getTransfersForUser(user.getName());
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public void processTransfer(@RequestBody Transfer transfer) throws InsufficientFundsException {
        dao.createTransfer(transfer);
    }

}
