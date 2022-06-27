package com.ebabu.event365live.host.bouncerecycler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BaseSpringSystem {
    private final Map<String, Spring> mSpringRegistry = new HashMap();
    private final Set<Spring> mActiveSprings = new CopyOnWriteArraySet();
    private final SpringLooper mSpringLooper;
    private final CopyOnWriteArraySet<SpringSystemListener> mListeners = new CopyOnWriteArraySet();
    private boolean mIdle = true;

    public BaseSpringSystem(SpringLooper springLooper) {
        if (springLooper == null) {
            throw new IllegalArgumentException("springLooper is required");
        } else {
            this.mSpringLooper = springLooper;
            this.mSpringLooper.setSpringSystem(this);
        }
    }

    public boolean getIsIdle() {
        return this.mIdle;
    }

    public Spring createSpring() {
        Spring spring = new Spring(this);
        this.registerSpring(spring);
        return spring;
    }

    public Spring getSpringById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        } else {
            return (Spring)this.mSpringRegistry.get(id);
        }
    }

    public List<Spring> getAllSprings() {
        Collection<Spring> collection = this.mSpringRegistry.values();
        Object list;
        if (collection instanceof List) {
            list = (List)collection;
        } else {
            list = new ArrayList(collection);
        }

        return Collections.unmodifiableList((List)list);
    }

    void registerSpring(Spring spring) {
        if (spring == null) {
            throw new IllegalArgumentException("spring is required");
        } else if (this.mSpringRegistry.containsKey(spring.getId())) {
            throw new IllegalArgumentException("spring is already registered");
        } else {
            this.mSpringRegistry.put(spring.getId(), spring);
        }
    }

    void deregisterSpring(Spring spring) {
        if (spring == null) {
            throw new IllegalArgumentException("spring is required");
        } else {
            this.mActiveSprings.remove(spring);
            this.mSpringRegistry.remove(spring.getId());
        }
    }

    void advance(double deltaTime) {
        Iterator i$ = this.mActiveSprings.iterator();

        while(i$.hasNext()) {
            Spring spring = (Spring)i$.next();
            if (spring.systemShouldAdvance()) {
                spring.advance(deltaTime / 1000.0D);
            } else {
                this.mActiveSprings.remove(spring);
            }
        }

    }

    public void loop(double ellapsedMillis) {
        Iterator i$ = this.mListeners.iterator();

        SpringSystemListener listener;
        while(i$.hasNext()) {
            listener = (SpringSystemListener)i$.next();
            listener.onBeforeIntegrate(this);
        }

        this.advance(ellapsedMillis);
        if (this.mActiveSprings.isEmpty()) {
            this.mIdle = true;
        }

        i$ = this.mListeners.iterator();

        while(i$.hasNext()) {
            listener = (SpringSystemListener)i$.next();
            listener.onAfterIntegrate(this);
        }

        if (this.mIdle) {
            this.mSpringLooper.stop();
        }

    }

    void activateSpring(String springId) {
        Spring spring = (Spring)this.mSpringRegistry.get(springId);
        if (spring == null) {
            throw new IllegalArgumentException("springId " + springId + " does not reference a registered spring");
        } else {
            this.mActiveSprings.add(spring);
            if (this.getIsIdle()) {
                this.mIdle = false;
                this.mSpringLooper.start();
            }

        }
    }

    public void addListener(SpringSystemListener newListener) {
        if (newListener == null) {
            throw new IllegalArgumentException("newListener is required");
        } else {
            this.mListeners.add(newListener);
        }
    }

    public void removeListener(SpringSystemListener listenerToRemove) {
        if (listenerToRemove == null) {
            throw new IllegalArgumentException("listenerToRemove is required");
        } else {
            this.mListeners.remove(listenerToRemove);
        }
    }

    public void removeAllListeners() {
        this.mListeners.clear();
    }
}
