package com.techelevator.tenmo.model;

public class Transfer {

    private int Id;
    private int accountFrom; //using account_id instead
    private int accountTo; //using account_id instead
    private String type;
    private String status;
    private String amount;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
