package com.ctnh.ctnhastral.registry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.ctnh.ctnhastral.CTNHAstral;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.HashMap;
import java.util.Map;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

public class CARocketBlocks {

    private static final String ROCKET_TEXTURE_ROOT = "block/rocket/";
    private static final Map<ResourceLocation, RocketPartStats> STATS = new HashMap<>();

    public static final BlockEntry<Block> BASIC_ROCKET_THRUSTER = registerThruster("basic_rocket_thruster",
            "基础火箭推进器", 1200);
    public static final BlockEntry<Block> BASIC_ROCKET_FUEL_TANK = registerFuelTank("basic_rocket_fuel_tank",
            "基础火箭燃料储罐", 16000);

    public static void init() {}

    public static BlockEntry<Block> registerThruster(String name, String cnName, int thrust) {
        return registerRocketPart(name, cnName, new RocketPartStats(thrust, 0));
    }

    public static BlockEntry<Block> registerFuelTank(String name, String cnName, long capacity) {
        return registerRocketPart(name, cnName, new RocketPartStats(0, capacity));
    }

    public static RocketPartStats getStats(Block block) {
        return STATS.getOrDefault(BuiltInRegistries.BLOCK.getKey(block), RocketPartStats.EMPTY);
    }

    private static BlockEntry<Block> registerRocketPart(String name, String cnName, RocketPartStats stats) {
        BlockEntry<Block> entry = REGISTRATE.block(name, Block::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), CTNHAstral.id(ROCKET_TEXTURE_ROOT + name))))
                .loot(RegistrateBlockLootTables::dropSelf)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
        STATS.put(CTNHAstral.id(name), stats);
        return entry;
    }

    public record RocketPartStats(int thrust, long fuelCapacity) {

        public static final RocketPartStats EMPTY = new RocketPartStats(0, 0);
    }

    private CARocketBlocks() {}
}
