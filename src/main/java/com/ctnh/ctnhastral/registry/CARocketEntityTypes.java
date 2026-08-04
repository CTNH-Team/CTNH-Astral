package com.ctnh.ctnhastral.registry;

import net.createmod.catnip.lang.Lang;
import net.minecraft.world.entity.MobCategory;

import com.ctnh.ctnhastral.common.entity.RocketContraptionEntity;
import com.mo_guang.ctpp.dynamicPart.SimpleContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

public class CARocketEntityTypes {

    public static final EntityEntry<RocketContraptionEntity> ROCKET_CONTRAPTION = ((CreateEntityBuilder<RocketContraptionEntity, ?>) REGISTRATE
            .movingEntity(Lang.asId("rocket_contraption"), RocketContraptionEntity::new, MobCategory.MISC)
            .properties(AbstractContraptionEntity::build)
            .properties(properties -> properties
                    .setTrackingRange(10)
                    .setUpdateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .fireImmune())
            .renderer(() -> SimpleContraptionEntityRenderer::new))
            .visual(() -> ContraptionVisual::new)
            .register();

    public static void init() {}
}
