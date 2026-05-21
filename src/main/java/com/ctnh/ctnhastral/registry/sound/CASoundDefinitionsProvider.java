package com.ctnh.ctnhastral.registry.sound;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

import com.ctnh.ctnhastral.CTNHAstral;

import static com.ctnh.ctnhastral.registry.sound.CASoundEvents.AMBIENT_ASTRAL;

public class CASoundDefinitionsProvider extends SoundDefinitionsProvider {

    public CASoundDefinitionsProvider(PackOutput output, String modId, ExistingFileHelper helper) {
        super(output, modId, helper);
    }

    @Override
    public void registerSounds() {
        this.add(AMBIENT_ASTRAL.get(), definition()
                .subtitle("subtitle.ctnhcore.bgm.plague_wasteland")
                .with(sound(CTNHAstral.id("astral_infection"))
                        .weight(3)
                        .volume(0.6)
                        .stream(),
                        sound(CTNHAstral.id("astral_infection_underground"))
                                .weight(3)
                                .volume(0.6)
                                .stream(),
                        sound(CTNHAstral.id("astrum_deus"))
                                .weight(1)
                                .volume(0.6)
                                .stream(),
                        sound(CTNHAstral.id("astrum_aureus"))
                                .weight(1)
                                .volume(0.6)
                                .stream()));
    }
}
