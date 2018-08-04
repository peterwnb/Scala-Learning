package com.today.enums;

public enum MemberSourceEnum {
    SOURCE_MEMBER("Master"), //来源会员主表
    SOURCE_CHANNEL("Slave");  //来源Channel表

    String val;

    MemberSourceEnum(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
