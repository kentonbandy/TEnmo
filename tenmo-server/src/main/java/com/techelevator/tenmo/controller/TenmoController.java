package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.dao.IllegalAccessError;
import com.techelevator.tenmo.model.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public List<TransferHistory> getTransfersForUser(Principal user) {
        return dao.getTransfersForUser(user.getName());
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public int processTransfer(@RequestBody TransferPayment transfer, Principal user)
            throws InsufficientFundsException, IllegalAccessError, NoSuchUserException {
        return dao.createTransfer(transfer, user.getName());
    }

    @RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
    public TransferDetails getTransferDetails(@PathVariable int transferId)
            throws NoSuchTransferIdException, NoSuchUserException {
        return dao.getTransferDetails(transferId);
    }

    @RequestMapping(path = "requests", method = RequestMethod.POST)
    public int processRequest(@RequestBody TransferPayment request, Principal user)
            throws IllegalAccessError, NoSuchUserException {
        return dao.createRequest(request, user.getName());
    }

    @RequestMapping(path = "pending", method = RequestMethod.GET)
    public List<TransferHistory> getPendingTransfersForUser(Principal user) {
        return dao.getPendingTransfersForUser(user.getName());
    }

    @RequestMapping(path = "/requests/{id}", method = RequestMethod.PUT)
    public int requestResponse(Principal user, @PathVariable int id, @RequestBody BigOlBoolean bool)
            throws NoSuchTransferIdException, InsufficientFundsException, NotPendingException {
        return dao.requestResponse(user.getName(), id, bool.isApproved());
    }


}
