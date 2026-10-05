package com.ctnh.ctnhastral.mixin.minecraft;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

import com.ctnh.ctnhastral.data.worldgen.CAWorldgen;
import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonTerrain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Inject(method = "applyCarvers", at = @At("RETURN"))
    private void ctnhastral$carveCanyons(WorldGenRegion level, long seed, RandomState random,
                                         BiomeManager biomes, StructureManager structures, ChunkAccess chunk,
                                         GenerationStep.Carving step, CallbackInfo ci) {
        if (step == GenerationStep.Carving.AIR &&
                ((NoiseBasedChunkGenerator) (Object) this).getBiomeSource() instanceof MoonBiomeSource moon) {
            MoonCanyonTerrain.carve(chunk, moon.canyons(), moon.canyonTerrain());
            moon.geology().apply(chunk);
        }
    }

    @Shadow
    @Final
    @Mutable
    private Supplier<Aquifer.FluidPicker> globalFluidPicker;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ctnhastral$bindFluidPicker(BiomeSource biomes, Holder<NoiseGeneratorSettings> settings,
                                            CallbackInfo ci) {
        Supplier<Aquifer.FluidPicker> picker = CAWorldgen.fluidPicker(settings);
        if (picker != null) globalFluidPicker = picker;
    }
}
