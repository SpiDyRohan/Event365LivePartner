package com.ebabu.event365live.host.events;

import androidx.lifecycle.LiveData;

public class SuccessEvent {
    LiveData<String> msg;
    public SuccessEvent(LiveData<String> msg) {
        this.msg = msg;
    }
    public LiveData<String> getMsg() {
        return msg;
    }
}
