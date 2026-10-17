package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Shared seeded terrain noise; each site owns its octave fields, with no mutable sampling state. */
public final class TerrainNoise {

    /** A dimension-supplied spectrum; coordinate warping changes outlines without independent pixel noise. */
    public record Field(double scaleX, double scaleY, double scaleZ, double warpScale, double warpStrength,
                        double detailScale, double detailWeight) {

        public static final Codec<Field> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.doubleRange(1, 2048).fieldOf("scale_x").forGetter(Field::scaleX),
                Codec.doubleRange(1, 256).fieldOf("scale_y").forGetter(Field::scaleY),
                Codec.doubleRange(1, 2048).fieldOf("scale_z").forGetter(Field::scaleZ),
                Codec.doubleRange(1, 2048).fieldOf("warp_scale").forGetter(Field::warpScale),
                Codec.doubleRange(0, 256).fieldOf("warp_strength").forGetter(Field::warpStrength),
                Codec.doubleRange(1, 256).fieldOf("detail_scale").forGetter(Field::detailScale),
                Codec.doubleRange(0, 1).fieldOf("detail_weight").forGetter(Field::detailWeight)).apply(i, Field::new));

        public Column column(TerrainNoise noise, double x, double z) {
            double dx = warpStrength * noise.shift(x / warpScale, z / warpScale);
            double dz = warpStrength * noise.shift(x / warpScale + 17, z / warpScale - 31);
            return new Column(noise, this, x + dx, z + dz);
        }
    }

    /** Bind horizontal coordinates once; volumetric layers reuse them through a whole block column. */
    public record Column(TerrainNoise noise, Field field, double x, double z) {

        public double value(double y) {
            return noise.body(x / field.scaleX(), y / field.scaleY(), z / field.scaleZ()) +
                    field.detailWeight() *
                            noise.detail(x / field.detailScale(), y / field.scaleY(), z / field.detailScale());
        }
    }

    private final long seed;
    private final PerlinNoise shift;
    private final PerlinNoise body;
    private final PerlinNoise detail;

    public TerrainNoise(long seed) {
        this.seed = seed;
        var factory = new XoroshiroRandomSource(seed).forkPositional();
        shift = PerlinNoise.create(factory.fromHashOf("ctnhastral:geology_shift"), 0, 1, .5, .2);
        body = PerlinNoise.create(factory.fromHashOf("ctnhastral:geology_body"), 0, 1, .5, .2);
        detail = PerlinNoise.create(factory.fromHashOf("ctnhastral:geology_detail"), 0, 1, .4);
    }

    public double shift(double x, double z) {
        return bounded(shift.getValue(x, 0, z));
    }

    public double body(double x, double y, double z) {
        return bounded(body.getValue(x, y, z));
    }

    public double detail(double x, double y, double z) {
        return bounded(detail.getValue(x, y, z));
    }

    private static double bounded(double value) {
        return Math.max(-1, Math.min(1, 2 * value));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TerrainNoise field && seed == field.seed;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(seed);
    }

    public static long mix(long x) {
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        return x ^ (x >>> 31);
    }

    public static double unit(long x) {
        return (mix(x) >>> 11) * 0x1.0p-53;
    }

    /** Continuous seeded value noise; neighboring columns and chunks share the same detail field. */
    public static double noise(long seed, double x, double z) {
        int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
        double tx = smooth(x - ix), tz = smooth(z - iz);
        return 2 * lerp(lerp(unit(seed ^ key(ix, iz)), unit(seed ^ key(ix + 1, iz)), tx),
                lerp(unit(seed ^ key(ix, iz + 1)), unit(seed ^ key(ix + 1, iz + 1)), tx), tz) - 1;
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static double clamp(double x) {
        return Math.max(0, Math.min(1, x));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double smooth(double x) {
        x = clamp(x);
        return x * x * (3 - 2 * x);
    }
}
