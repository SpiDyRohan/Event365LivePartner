package com.ebabu.event365live.host.bouncerecycler;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

public class Spring {
    private static int ID = 0;
    private static final double MAX_DELTA_TIME_SEC = 0.064D;
    private static final double SOLVER_TIMESTEP_SEC = 0.001D;
    private SpringConfig mSpringConfig;
    private boolean mOvershootClampingEnabled;
    private final String mId;
    private final PhysicsState mCurrentState = new PhysicsState();
    private final PhysicsState mPreviousState = new PhysicsState();
    private final PhysicsState mTempState = new PhysicsState();
    private double mStartValue;
    private double mEndValue;
    private boolean mWasAtRest = true;
    private double mRestSpeedThreshold = 0.005D;
    private double mDisplacementFromRestThreshold = 0.005D;
    private CopyOnWriteArraySet<SpringListener> mListeners = new CopyOnWriteArraySet();
    private double mTimeAccumulator = 0.0D;
    private final BaseSpringSystem mSpringSystem;

    Spring(BaseSpringSystem springSystem) {
        if (springSystem == null) {
            throw new IllegalArgumentException("Spring cannot be created outside of a BaseSpringSystem");
        } else {
            this.mSpringSystem = springSystem;
            this.mId = "spring:" + ID++;
            this.setSpringConfig(SpringConfig.defaultConfig);
        }
    }

    public void destroy() {
        this.mListeners.clear();
        this.mSpringSystem.deregisterSpring(this);
    }

    public String getId() {
        return this.mId;
    }

    public Spring setSpringConfig(SpringConfig springConfig) {
        if (springConfig == null) {
            throw new IllegalArgumentException("springConfig is required");
        } else {
            this.mSpringConfig = springConfig;
            return this;
        }
    }

    public SpringConfig getSpringConfig() {
        return this.mSpringConfig;
    }

    public Spring setCurrentValue(double currentValue) {
        return this.setCurrentValue(currentValue, true);
    }

    public Spring setCurrentValue(double currentValue, boolean setAtRest) {
        this.mStartValue = currentValue;
        this.mCurrentState.position = currentValue;
        this.mSpringSystem.activateSpring(this.getId());
        Iterator i$ = this.mListeners.iterator();

        while(i$.hasNext()) {
            SpringListener listener = (SpringListener)i$.next();
            listener.onSpringUpdate(this);
        }

        if (setAtRest) {
            this.setAtRest();
        }

        return this;
    }

    public double getStartValue() {
        return this.mStartValue;
    }

    public double getCurrentValue() {
        return this.mCurrentState.position;
    }

    public double getCurrentDisplacementDistance() {
        return this.getDisplacementDistanceForState(this.mCurrentState);
    }

    private double getDisplacementDistanceForState(PhysicsState state) {
        return Math.abs(this.mEndValue - state.position);
    }

    public Spring setEndValue(double endValue) {
        if (this.mEndValue == endValue && this.isAtRest()) {
            return this;
        } else {
            this.mStartValue = this.getCurrentValue();
            this.mEndValue = endValue;
            this.mSpringSystem.activateSpring(this.getId());
            Iterator i$ = this.mListeners.iterator();

            while(i$.hasNext()) {
                SpringListener listener = (SpringListener)i$.next();
                listener.onSpringEndStateChange(this);
            }

            return this;
        }
    }

    public double getEndValue() {
        return this.mEndValue;
    }

    public Spring setVelocity(double velocity) {
        if (velocity == this.mCurrentState.velocity) {
            return this;
        } else {
            this.mCurrentState.velocity = velocity;
            this.mSpringSystem.activateSpring(this.getId());
            return this;
        }
    }

    public double getVelocity() {
        return this.mCurrentState.velocity;
    }

    public Spring setRestSpeedThreshold(double restSpeedThreshold) {
        this.mRestSpeedThreshold = restSpeedThreshold;
        return this;
    }

    public double getRestSpeedThreshold() {
        return this.mRestSpeedThreshold;
    }

    public Spring setRestDisplacementThreshold(double displacementFromRestThreshold) {
        this.mDisplacementFromRestThreshold = displacementFromRestThreshold;
        return this;
    }

    public double getRestDisplacementThreshold() {
        return this.mDisplacementFromRestThreshold;
    }

    public Spring setOvershootClampingEnabled(boolean overshootClampingEnabled) {
        this.mOvershootClampingEnabled = overshootClampingEnabled;
        return this;
    }

    public boolean isOvershootClampingEnabled() {
        return this.mOvershootClampingEnabled;
    }

    public boolean isOvershooting() {
        return this.mSpringConfig.tension > 0.0D && (this.mStartValue < this.mEndValue && this.getCurrentValue() > this.mEndValue || this.mStartValue > this.mEndValue && this.getCurrentValue() < this.mEndValue);
    }

    void advance(double realDeltaTime) {
        boolean isAtRest = this.isAtRest();
        if (!isAtRest || !this.mWasAtRest) {
            double adjustedDeltaTime = realDeltaTime;
            if (realDeltaTime > 0.064D) {
                adjustedDeltaTime = 0.064D;
            }

            this.mTimeAccumulator += adjustedDeltaTime;
            double tension = this.mSpringConfig.tension;
            double friction = this.mSpringConfig.friction;
            double position = this.mCurrentState.position;
            double velocity = this.mCurrentState.velocity;
            double tempPosition = this.mTempState.position;

            double dvdt;
            double tempVelocity;
            for(tempVelocity = this.mTempState.velocity; this.mTimeAccumulator >= 0.001D; velocity += dvdt * 0.001D) {
                this.mTimeAccumulator -= 0.001D;
                if (this.mTimeAccumulator < 0.001D) {
                    this.mPreviousState.position = position;
                    this.mPreviousState.velocity = velocity;
                }

                double aAcceleration = tension * (this.mEndValue - tempPosition) - friction * velocity;
                tempPosition = position + velocity * 0.001D * 0.5D;
                tempVelocity = velocity + aAcceleration * 0.001D * 0.5D;
                double bVelocity = tempVelocity;
                double bAcceleration = tension * (this.mEndValue - tempPosition) - friction * tempVelocity;
                tempPosition = position + tempVelocity * 0.001D * 0.5D;
                tempVelocity = velocity + bAcceleration * 0.001D * 0.5D;
                double cVelocity = tempVelocity;
                double cAcceleration = tension * (this.mEndValue - tempPosition) - friction * tempVelocity;
                tempPosition = position + tempVelocity * 0.001D;
                tempVelocity = velocity + cAcceleration * 0.001D;
                double dAcceleration = tension * (this.mEndValue - tempPosition) - friction * tempVelocity;
                double dxdt = 0.16666666666666666D * (velocity + 2.0D * (bVelocity + cVelocity) + tempVelocity);
                dvdt = 0.16666666666666666D * (aAcceleration + 2.0D * (bAcceleration + cAcceleration) + dAcceleration);
                position += dxdt * 0.001D;
            }

            this.mTempState.position = tempPosition;
            this.mTempState.velocity = tempVelocity;
            this.mCurrentState.position = position;
            this.mCurrentState.velocity = velocity;
            if (this.mTimeAccumulator > 0.0D) {
                this.interpolate(this.mTimeAccumulator / 0.001D);
            }

            if (this.isAtRest() || this.mOvershootClampingEnabled && this.isOvershooting()) {
                if (tension > 0.0D) {
                    this.mStartValue = this.mEndValue;
                    this.mCurrentState.position = this.mEndValue;
                } else {
                    this.mEndValue = this.mCurrentState.position;
                    this.mStartValue = this.mEndValue;
                }

                this.setVelocity(0.0D);
                isAtRest = true;
            }

            boolean notifyActivate = false;
            if (this.mWasAtRest) {
                this.mWasAtRest = false;
                notifyActivate = true;
            }

            boolean notifyAtRest = false;
            if (isAtRest) {
                this.mWasAtRest = true;
                notifyAtRest = true;
            }

            Iterator i$ = this.mListeners.iterator();

            while(i$.hasNext()) {
                SpringListener listener = (SpringListener)i$.next();
                if (notifyActivate) {
                    listener.onSpringActivate(this);
                }

                listener.onSpringUpdate(this);
                if (notifyAtRest) {
                    listener.onSpringAtRest(this);
                }
            }

        }
    }

    public boolean systemShouldAdvance() {
        return !this.isAtRest() || !this.wasAtRest();
    }

    public boolean wasAtRest() {
        return this.mWasAtRest;
    }

    public boolean isAtRest() {
        return Math.abs(this.mCurrentState.velocity) <= this.mRestSpeedThreshold && (this.getDisplacementDistanceForState(this.mCurrentState) <= this.mDisplacementFromRestThreshold || this.mSpringConfig.tension == 0.0D);
    }

    public Spring setAtRest() {
        this.mEndValue = this.mCurrentState.position;
        this.mTempState.position = this.mCurrentState.position;
        this.mCurrentState.velocity = 0.0D;
        return this;
    }

    private void interpolate(double alpha) {
        this.mCurrentState.position = this.mCurrentState.position * alpha + this.mPreviousState.position * (1.0D - alpha);
        this.mCurrentState.velocity = this.mCurrentState.velocity * alpha + this.mPreviousState.velocity * (1.0D - alpha);
    }

    public Spring addListener(SpringListener newListener) {
        if (newListener == null) {
            throw new IllegalArgumentException("newListener is required");
        } else {
            this.mListeners.add(newListener);
            return this;
        }
    }

    public Spring removeListener(SpringListener listenerToRemove) {
        if (listenerToRemove == null) {
            throw new IllegalArgumentException("listenerToRemove is required");
        } else {
            this.mListeners.remove(listenerToRemove);
            return this;
        }
    }

    public Spring removeAllListeners() {
        this.mListeners.clear();
        return this;
    }

    public boolean currentValueIsApproximately(double value) {
        return Math.abs(this.getCurrentValue() - value) <= this.getRestDisplacementThreshold();
    }

    private static class PhysicsState {
        double position;
        double velocity;

        private PhysicsState() {
        }
    }
}
