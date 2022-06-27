package com.ebabu.event365live.host.entities;

import java.io.Serializable;

public class EventImages implements Serializable {

    private int id;
    private String eventImage;
    private boolean isPrimary;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventImage() {
        return eventImage;
    }
    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Override
    public String toString() {
        return "EventImages{" +
                "eventImage='" + eventImage + '\'' +
                '}';
    }
}
