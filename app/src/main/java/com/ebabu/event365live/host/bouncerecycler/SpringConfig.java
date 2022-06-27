package com.ebabu.event365live.host.bouncerecycler;

public class SpringConfig {
    public double friction;
    public double tension;
    public static SpringConfig defaultConfig = fromOrigamiTensionAndFriction(40.0D, 7.0D);

    public SpringConfig(double tension, double friction) {
        this.tension = tension;
        this.friction = friction;
    }

    public static SpringConfig fromOrigamiTensionAndFriction(double qcTension, double qcFriction) {
        return new SpringConfig(OrigamiValueConverter.tensionFromOrigamiValue(qcTension), OrigamiValueConverter.frictionFromOrigamiValue(qcFriction));
    }

    public static SpringConfig fromBouncinessAndSpeed(double bounciness, double speed) {
        BouncyConversion bouncyConversion = new BouncyConversion(speed, bounciness);
        return fromOrigamiTensionAndFriction(bouncyConversion.getBouncyTension(), bouncyConversion.getBouncyFriction());
    }
}
