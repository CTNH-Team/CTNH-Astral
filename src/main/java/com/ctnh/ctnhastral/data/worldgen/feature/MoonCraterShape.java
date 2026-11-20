package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.util.Mth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Simple lunar bowls, with game-scale rim uplift kept separate from excavation depth. */
public record MoonCraterShape(double radius, double preservation, double phase, double excavation,
                              double rim, double innerJoin, double outerJoin) {

    static final double FOOTPRINT_FACTOR = 2.5;
    static final double OUTLINE_VARIATION = 0.06;

    /** Tunable size distribution and depth; the profile mathematics stays in this class. */
    public record Settings(double minDiameter, double maxDiameter, double filledChance,
                           double minRim, double depthBase, double depthExponent) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("min_diameter").forGetter(Settings::minDiameter),
                Codec.DOUBLE.fieldOf("max_diameter").forGetter(Settings::maxDiameter),
                Codec.DOUBLE.fieldOf("filled_chance").forGetter(Settings::filledChance),
                Codec.DOUBLE.fieldOf("min_rim").forGetter(Settings::minRim),
                Codec.DOUBLE.fieldOf("depth_base").forGetter(Settings::depthBase),
                Codec.DOUBLE.fieldOf("depth_exponent").forGetter(Settings::depthExponent))
                .apply(instance, Settings::new));

        double maxFootprint() {
            return maxDiameter / 2 * FOOTPRINT_FACTOR * (1 + OUTLINE_VARIATION);
        }
    }

    static MoonCraterShape create(long seed, Settings settings) {
        // Truncated D^-3 distribution: small landmarks are more common than large ones.
        double diameter = 1 / Math.sqrt(1 / (settings.minDiameter() * settings.minDiameter()) - unit(seed) *
                (1 / (settings.minDiameter() * settings.minDiameter()) -
                        1 / (settings.maxDiameter() * settings.maxDiameter())));
        boolean filled = unit(seed + 4) < settings.filledChance();
        double preservation = unit(seed + 1) * (filled ? 0.2 : 1);
        double depthSample = unit(seed + 2);
        double referenceRim = 0.04 * diameter * (0.15 + 0.85 * preservation);
        double rim = Math.max(settings.minRim(), 1.6 * referenceRim);
        // Rim-to-floor depth is exaggerated for gameplay and independent of surface preservation.
        // Filled craters retain a shallow excavation and a worn, sediment-rich surface.
        double depth = filled ? rim + diameter * (0.02 + 0.02 * depthSample) :
                settings.depthBase() * Math.pow(diameter / settings.minDiameter(), settings.depthExponent()) *
                        (0.9 + 0.2 * depthSample);
        double radius = diameter / 2;
        double outerWidth = Math.max((0.35 + 0.30 * (1 - preservation)) * radius, 2.8 * rim);
        return new MoonCraterShape(radius, preservation, unit(seed + 3) * Math.PI * 2,
                depth - rim, rim, 0.90 - 0.18 * (1 - preservation),
                1 + outerWidth / radius);
    }

    double footprint() {
        return radius * FOOTPRINT_FACTOR * (1 + OUTLINE_VARIATION);
    }

    double normalizedRadius(double dx, double dz) {
        double angle = Math.atan2(dz, dx);
        double localRadius = radius * (1 + 0.04 * Math.sin(3 * angle + phase) +
                0.02 * Math.sin(5 * angle - phase));
        return Math.hypot(dx, dz) / localRadius;
    }

    double profile(double rho) {
        double depth = excavation + rim;
        if (rho <= innerJoin) {
            return -excavation + depth * rho * rho / innerJoin;
        }
        if (rho <= 1) {
            return rim - depth * (1 - rho) * (1 - rho) / (1 - innerJoin);
        }
        double ejecta = 0.5 * rim;
        if (rho <= outerJoin) {
            double width = outerJoin - 1;
            double t = (rho - 1) / width;
            double endHeight = ejecta / Math.pow(outerJoin, 3);
            double endSlope = -3 * ejecta / Math.pow(outerJoin, 4);
            return (2 * t * t * t - 3 * t * t + 1) * rim +
                    (-2 * t * t * t + 3 * t * t) * endHeight +
                    (t * t * t - t * t) * width * endSlope;
        }
        if (rho < FOOTPRINT_FACTOR) {
            return ejecta / (rho * rho * rho) * (1 - smoothstep((rho - 2) / 0.5));
        }
        return 0;
    }

    double roughness(long seed, int x, int z, double rho) {
        double brokenRock = 2 * noise(seed, x, z, 6) - 1;
        // Surface chips must not cut down the primary rim's five-block minimum.
        brokenRock = Math.max(0, brokenRock) + Math.min(0, brokenRock) *
                smoothstep(Math.abs(rho - 1) / 0.10);
        double envelope = smoothstep(rho / 0.5) * (1 - smoothstep((rho - 1.5) / 0.8));
        return brokenRock * 1.6 * (0.25 + 0.75 * preservation) * envelope;
    }

    double debrisProbability(double rho) {
        double envelope = smoothstep((rho - 1.02) / 0.12) *
                (1 - smoothstep((rho - 1.8) / 0.5));
        double distance = (rho - 1.3) / 0.3;
        return (0.08 + 0.16 * preservation) * Math.exp(-distance * distance) * envelope;
    }

    static double smoothstep(double value) {
        double t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    static double noise(long seed, int x, int z, int scale) {
        int cellX = Math.floorDiv(x, scale);
        int cellZ = Math.floorDiv(z, scale);
        double tx = smoothstep(Math.floorMod(x, scale) / (double) scale);
        double tz = smoothstep(Math.floorMod(z, scale) / (double) scale);
        return Mth.lerp(tz,
                Mth.lerp(tx, unit(seedAt(seed, cellX, cellZ)), unit(seedAt(seed, cellX + 1, cellZ))),
                Mth.lerp(tx, unit(seedAt(seed, cellX, cellZ + 1)), unit(seedAt(seed, cellX + 1, cellZ + 1))));
    }

    static long seedAt(long seed, int x, int z) {
        return mix(seed ^ (long) x * 0x632BE59BD9B4E019L ^ (long) z * 0x9E3779B97F4A7C15L);
    }

    static double unit(long seed) {
        return (mix(seed) >>> 11) * 0x1.0p-53;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
