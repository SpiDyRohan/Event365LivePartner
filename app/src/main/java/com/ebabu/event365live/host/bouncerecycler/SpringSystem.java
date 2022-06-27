package com.ebabu.event365live.host.bouncerecycler;

public class SpringSystem extends BaseSpringSystem {
    public static SpringSystem create() {
        return new SpringSystem(AndroidSpringLooperFactory.createSpringLooper());
    }

    private SpringSystem(SpringLooper springLooper) {
        super(springLooper);
    }
}
