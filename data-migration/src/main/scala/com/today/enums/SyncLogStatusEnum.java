package com.today.enums;

public enum SyncLogStatusEnum {

    WAIT(0),
    SUCCESS(1),
    FAILED(2);
    int val;

    SyncLogStatusEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
