package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.MethodDescription;
import net.minecraft.util.Mth;

import java.util.Random;

@SuppressWarnings("unused")
public class MathApi {

    public final RadiansApi radians = new RadiansApi();
    public final DegreesApi degrees = new DegreesApi();

    private final Random random = new Random();

    @MethodDescription("""
            Random number in [0..1) range
            """)
    public double random() {
        return random.nextDouble();
    }

    @MethodDescription("""
            Random integer number in [0..max) range
            """)
    public int randomInt(int max) {
        if (max <= 0) {
            return Integer.MIN_VALUE;
        }

        return random.nextInt(max);
    }

    @MethodDescription("""
            Random number in [min..max) range
            """)
    public int randomInt(int min, int max) {
        if (min >= max) {
            return Integer.MIN_VALUE;
        }

        return random.nextInt(min, max);
    }

    public int round(double value) {
        return (int) Math.round(value);
    }

    public int floor(double value) {
        return Mth.floor(value);
    }

    public int floorDiv(int x, int y) {
        return Math.floorDiv(x, y);
    }

    public int floorMod(int x, int y) {
        return Math.floorMod(x, y);
    }

    public int ceil(double value) {
        return Mth.ceil(value);
    }

    public int abs(int value) {
        return Math.abs(value);
    }

    public double abs(double value) {
        return Math.abs(value);
    }

    public double sqrt(double value) {
        return Math.sqrt(value);
    }

    public int max(int v1, int v2) {
        return Math.max(v1, v2);
    }

    public int min(int v1, int v2) {
        return Math.min(v1, v2);
    }

    public double max(double v1, double v2) {
        return Math.max(v1, v2);
    }

    public double min(double v1, double v2) {
        return Math.min(v1, v2);
    }

    public static class RadiansApi {

        public double sin(double value) {
            return Math.sin(value);
        }

        public double cos(double value) {
            return Math.cos(value);
        }

        public double tan(double value) {
            return Math.tan(value);
        }

        public double asin(double value) {
            return Math.asin(value);
        }

        public double acos(double value) {
            return Math.asin(value);
        }

        public double atan(double value) {
            return Math.atan(value);
        }

        public double atan2(double y, double x) {
            return Math.atan2(y, x);
        }

        public double toDegrees(double value) {
            return Math.toDegrees(value);
        }
    }

    public static class DegreesApi {

        public double sin(double value) {
            return Math.sin(Math.toRadians(value));
        }

        public double cos(double value) {
            return Math.cos(Math.toRadians(value));
        }

        public double tan(double value) {
            return Math.tan(Math.toRadians(value));
        }

        public double asin(double value) {
            return Math.toDegrees(Math.asin(value));
        }

        public double acos(double value) {
            return Math.toDegrees(Math.asin(value));
        }

        public double atan(double value) {
            return Math.toDegrees(Math.atan(value));
        }

        public double atan2(double y, double x) {
            return Math.toDegrees(Math.atan2(y, x));
        }

        public double toRadians(double value) {
            return Math.toRadians(value);
        }
    }
}