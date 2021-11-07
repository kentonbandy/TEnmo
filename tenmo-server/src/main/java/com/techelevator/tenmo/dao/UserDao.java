package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.*;

import java.util.List;

public interface UserDao {

    List<User> findAll();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    double getBalanceByUsername(String username);

    int createTransfer(TransferPayment transfer, String username) throws InsufficientFundsException, IllegalAccessError, NoSuchUserException;

    List<TransferHistory> getTransfersForUser(String user);

    List<User> getAllUsers();

    TransferDetails getTransferDetails(int id) throws NoSuchTransferIdException, NoSuchUserException;

    int createRequest(TransferPayment request, String username) throws NoSuchUserException, IllegalAccessError;

    List<TransferHistory> getPendingTransfersForUser(String username);

    int requestResponse(String username, int transferId, boolean isApproved) throws NoSuchTransferIdException, InsufficientFundsException, NotPendingException;
}
