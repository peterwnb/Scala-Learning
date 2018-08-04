package com.today.enums;

public enum  MemberCardAccountStatusEnum {

    NORMAL(1),

    FREEZE(2);

    int val;

    MemberCardAccountStatusEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
