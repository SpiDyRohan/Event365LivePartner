package com.ebabu.event365live.host.bouncerecycler;

public abstract class SpringLooper {
    protected BaseSpringSystem mSpringSystem;

    public SpringLooper() {
    }

    public void setSpringSystem(BaseSpringSystem springSystem) {
        this.mSpringSystem = springSystem;
    }

    public abstract void start();

    public abstract void stop();
}
