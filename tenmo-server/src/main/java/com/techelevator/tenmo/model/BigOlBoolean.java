package com.techelevator.tenmo.model;

import javax.validation.constraints.NotNull;

public class BigOlBoolean {
    @NotNull
    private boolean isApproved;

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}