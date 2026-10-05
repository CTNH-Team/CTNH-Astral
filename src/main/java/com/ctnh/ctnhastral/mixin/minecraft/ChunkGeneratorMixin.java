package com.ctnh.ctnhastral.mixin.minecraft;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "createState", at = @At("HEAD"))
    private void ctnhastral$initializeMoonLayouts(HolderLookup<StructureSet> structureSets, RandomState randomState,
                                                  long seed, CallbackInfoReturnable<ChunkGeneratorStructureState> cir) {
        if ((Object) this instanceof NoiseBasedChunkGenerator generator &&
                generator.getBiomeSource() instanceof MoonBiomeSource moon) {
            moon.initialize(generator, randomState, seed);
        }
    }
}
