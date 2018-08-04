package com.today.enums;

public enum LogTypeEnum {

    MEMBER_MEMBER_BASE(1),
    ORDER_ORDER_BASE(2),
    CARD_CARD_BASE(3),
    CARD_CARD_CONSUME(4),
    MEMBER_MEMBER_COUPON(5),
    MEMBER_SCORE_HISTORY(6);
    int val;

    LogTypeEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
