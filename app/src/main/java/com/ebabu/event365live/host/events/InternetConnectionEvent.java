package com.ebabu.event365live.host.events;

public class InternetConnectionEvent {
    boolean isConnected;

    public InternetConnectionEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
