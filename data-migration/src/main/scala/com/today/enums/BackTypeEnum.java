package com.today.enums;

/**
 * 1:card_base 2:member_coupon 3:member_order 4:card_consume 5:score_journal',
 */
public enum BackTypeEnum {

    CARD_BASE(1),
    MEMBER_COUPON(2),
    MEMBER_ORDER(3),
    CARD_CONSUME(4),
    SCORE_JOURNAL(5);
    int val;

    BackTypeEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
