package com.today.enums;

public enum CardStatusEnum {

    HAVE_DONATION(1),
    IN_THE_DONATION(2),
    IN_USE(3),
    USE_FINISHED(4),
    BLACK_CARD(5),
    HAS_BE_CONVERT(6),
    TO_BE_RECEIVED(7),
    TO_BE_RECEIVED_OLD(9),
    TO_BE_SALE_OLD(10),
    TO_BE_LOSS_OLD(11);

    int val;

    CardStatusEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
