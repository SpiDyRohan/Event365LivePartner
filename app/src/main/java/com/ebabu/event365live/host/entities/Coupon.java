package com.ebabu.event365live.host.entities;

import java.io.Serializable;

public class Coupon implements Serializable {
    private String coupanCode;

    public String getCouponCode() {
        return coupanCode;
    }

    public void setCouponCode(String coupanCode) {
        this.coupanCode = coupanCode;
    }
}
