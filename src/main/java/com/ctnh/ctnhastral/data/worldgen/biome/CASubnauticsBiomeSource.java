package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

public class CASubnauticsBiomeSource extends BiomeSource {

    public static final Codec<CASubnauticsBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BIOME)
                    .xmap(biomes -> biomes.stream().findFirst().orElseThrow(), HolderSet::direct)
                    .fieldOf("shallow_biome").forGetter(source -> source.shallowBiome),
            RegistryCodecs.homogeneousList(Registries.BIOME)
                    .xmap(biomes -> biomes.stream().findFirst().orElseThrow(), HolderSet::direct)
                    .fieldOf("seagrass_biome").forGetter(source -> source.seagrassBiome),
            RegistryCodecs.homogeneousList(Registries.BIOME)
                    .xmap(biomes -> biomes.stream().findFirst().orElseThrow(), HolderSet::direct)
                    .fieldOf("red_algae_biome").forGetter(source -> source.redAlgaeBiome))
            .apply(instance, CASubnauticsBiomeSource::new));

    private final Holder<Biome> shallowBiome;
    private final Holder<Biome> seagrassBiome;
    private final Holder<Biome> redAlgaeBiome;

    public CASubnauticsBiomeSource(Holder<Biome> shallowBiome, Holder<Biome> seagrassBiome,
                                   Holder<Biome> redAlgaeBiome) {
        this.shallowBiome = shallowBiome;
        this.seagrassBiome = seagrassBiome;
        this.redAlgaeBiome = redAlgaeBiome;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(shallowBiome, seagrassBiome, redAlgaeBiome);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockY = y * 4;
        if (blockY <= -110 && blockY >= -150) {
            return redAlgaeBiome;
        }
        if (blockY <= -80 && blockY >= -100) {
            return seagrassBiome;
        }
        return shallowBiome;
    }
}
