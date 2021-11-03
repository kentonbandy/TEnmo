package com.techelevator.tenmo.model;

public class Transfer {
    private int Id;
    private BasicUser from;
    private BasicUser to;
    private String type;
    private String status;
    private String amount;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public BasicUser getFrom() {
        return from;
    }

    public void setFrom(BasicUser from) {
        this.from = from;
    }

    public BasicUser getTo() {
        return to;
    }

    public void setTo(BasicUser to) {
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
