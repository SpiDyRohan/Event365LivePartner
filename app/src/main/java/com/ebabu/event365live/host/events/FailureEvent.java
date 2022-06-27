package com.ebabu.event365live.host.events;

public class FailureEvent{
    String msg;

    public FailureEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
