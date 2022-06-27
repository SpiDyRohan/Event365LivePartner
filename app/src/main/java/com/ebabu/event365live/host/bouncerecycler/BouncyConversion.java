package com.ebabu.event365live.host.bouncerecycler;

public class BouncyConversion {
    private final double mBouncyTension;
    private final double mBouncyFriction;
    private final double mSpeed;
    private final double mBounciness;

    public BouncyConversion(double speed, double bounciness) {
        this.mSpeed = speed;
        this.mBounciness = bounciness;
        double b = this.normalize(bounciness / 1.7D, 0.0D, 20.0D);
        b = this.project_normal(b, 0.0D, 0.8D);
        double s = this.normalize(speed / 1.7D, 0.0D, 20.0D);
        this.mBouncyTension = this.project_normal(s, 0.5D, 200.0D);
        this.mBouncyFriction = this.quadratic_out_interpolation(b, this.b3_nobounce(this.mBouncyTension), 0.01D);
    }

    public double getSpeed() {
        return this.mSpeed;
    }

    public double getBounciness() {
        return this.mBounciness;
    }

    public double getBouncyTension() {
        return this.mBouncyTension;
    }

    public double getBouncyFriction() {
        return this.mBouncyFriction;
    }

    private double normalize(double value, double startValue, double endValue) {
        return (value - startValue) / (endValue - startValue);
    }

    private double project_normal(double n, double start, double end) {
        return start + n * (end - start);
    }

    private double linear_interpolation(double t, double start, double end) {
        return t * end + (1.0D - t) * start;
    }

    private double quadratic_out_interpolation(double t, double start, double end) {
        return this.linear_interpolation(2.0D * t - t * t, start, end);
    }

    private double b3_friction1(double x) {
        return 7.0E-4D * Math.pow(x, 3.0D) - 0.031D * Math.pow(x, 2.0D) + 0.64D * x + 1.28D;
    }

    private double b3_friction2(double x) {
        return 4.4E-5D * Math.pow(x, 3.0D) - 0.006D * Math.pow(x, 2.0D) + 0.36D * x + 2.0D;
    }

    private double b3_friction3(double x) {
        return 4.5E-7D * Math.pow(x, 3.0D) - 3.32E-4D * Math.pow(x, 2.0D) + 0.1078D * x + 5.84D;
    }

    private double b3_nobounce(double tension) {
        double friction = 0.0D;
        if (tension <= 18.0D) {
            friction = this.b3_friction1(tension);
        } else if (tension > 18.0D && tension <= 44.0D) {
            friction = this.b3_friction2(tension);
        } else if (tension > 44.0D) {
            friction = this.b3_friction3(tension);
        } else {
            assert false;
        }

        return friction;
    }
}
