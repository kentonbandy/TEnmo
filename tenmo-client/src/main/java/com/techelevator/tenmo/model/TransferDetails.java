package com.techelevator.tenmo.model;

public class TransferDetails {

    private int transferId;
    private String from;
    private String to;
    private String type;
    private String status;
    private double amount;

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        System.out.println("Id: " + transferId);
        System.out.println("From: " + from);
        System.out.println("To: " + to);
        System.out.println("Type: " + type);
        System.out.println("Status: " + status);
        System.out.println("Amount: $" + amount);
        return "";
    }
}
