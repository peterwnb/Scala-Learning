package com.today.enums;

public enum LogSourceEnum {

    INCREASE(1),
    ALL(2);
    int val;

    LogSourceEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
