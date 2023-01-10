package com.mryan.mdex.until;

/**
 * @description： 加固的类型
 * @Author MRyan
 * @Date 2020/6/6 22:02
 * @Version 1.0
 */
public enum ReinforceType {
    QIHOO("360加固"),
    TENCENT("腾讯御加固"),
    AIJIAMI("爱加密"),
    BANGBANG("梆梆加固"),
    BAIDU("百度加固");

    String type;

    ReinforceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
