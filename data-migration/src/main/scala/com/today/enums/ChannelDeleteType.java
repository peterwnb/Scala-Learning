package com.today.enums;

public enum ChannelDeleteType {
    INVALID_MEMBER_ID(1), //不存在的memberId
    SAME_OPENID(2),  //memberId相同，且有相同的OpenId
    MEMBER_NO_OPEN_ID(3);  //memberId存在，但是主表没有we_chat_open_id

    int val;

    ChannelDeleteType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
