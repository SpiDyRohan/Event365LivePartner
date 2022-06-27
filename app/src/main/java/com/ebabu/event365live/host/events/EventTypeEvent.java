package com.ebabu.event365live.host.events;

public class EventTypeEvent {
    private int num;

    public int getNum() {
        return num;
    }

    public EventTypeEvent(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "EventTypeEvent{" +
                "num=" + num +
                '}';
    }
}
