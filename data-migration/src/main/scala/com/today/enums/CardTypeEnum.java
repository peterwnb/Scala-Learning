package com.today.enums;

public enum CardTypeEnum {
    NORMAL(1),
    VIRTUAL(2);

    int val;

    CardTypeEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
