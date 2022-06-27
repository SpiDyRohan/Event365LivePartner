package com.ebabu.event365live.host.events;

public class EventCreatePermissionEvent{
    boolean isCreateEvent,isManageUser;

    public boolean isCreateEvent() {
        return isCreateEvent;
    }

    public boolean isManageUser() {
        return isManageUser;
    }

    public EventCreatePermissionEvent(boolean isCreateEvent, boolean isManageUser) {
        this.isCreateEvent = isCreateEvent;
        this.isManageUser = isManageUser;
    }
}
