package com.techelevator.tenmo.model;

import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotEmpty;

public class Transfer {

    @NumberFormat
    private int id;
    @NumberFormat
    private int from;
    @NumberFormat
    private int to;
    @NumberFormat
    private double amount;
    @NotEmpty
    private String type;
    @NotEmpty
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
}
