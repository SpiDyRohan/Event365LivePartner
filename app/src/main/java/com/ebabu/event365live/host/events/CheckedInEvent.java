package com.ebabu.event365live.host.events;

public class CheckedInEvent {
    String query;

    public CheckedInEvent(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "CheckedInEvent{" +
                "query='" + query + '\'' +
                '}';
    }

    public String getQuery() {
        return query;
    }
}
