package com.ebabu.event365live.host.bouncerecycler;

public class OrigamiValueConverter  {
    public OrigamiValueConverter() {
    }

    public static double tensionFromOrigamiValue(double oValue) {
        return oValue == 0.0D ? 0.0D : (oValue - 30.0D) * 3.62D + 194.0D;
    }

    public static double origamiValueFromTension(double tension) {
        return tension == 0.0D ? 0.0D : (tension - 194.0D) / 3.62D + 30.0D;
    }

    public static double frictionFromOrigamiValue(double oValue) {
        return oValue == 0.0D ? 0.0D : (oValue - 8.0D) * 3.0D + 25.0D;
    }

    public static double origamiValueFromFriction(double friction) {
        return friction == 0.0D ? 0.0D : (friction - 25.0D) / 3.0D + 8.0D;
    }
}
