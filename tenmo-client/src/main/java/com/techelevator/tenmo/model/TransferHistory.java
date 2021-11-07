package com.techelevator.tenmo.model;

public class TransferHistory {

    private int transferId;
    private boolean isFrom;
    private String username;
    private double amount;

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public boolean isFrom() {
        return isFrom;
    }

    public void setFrom(boolean from) {
        isFrom = from;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        if(isFrom) {
            return transferId + "    From: " + username +  "            $" + amount;
        }
        return transferId + "    To: " + username +  "          $" + amount;
    }
}
