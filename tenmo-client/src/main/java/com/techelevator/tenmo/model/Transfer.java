package com.techelevator.tenmo.model;

public class Transfer {

    private int id;
    private int from; //using account_id instead
    private int to; //using account_id instead
    private String type;
    private String status;
    private String amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        id = id;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
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
