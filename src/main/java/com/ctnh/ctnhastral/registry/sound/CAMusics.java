package com.ctnh.ctnhastral.registry.sound;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;

import static com.ctnh.ctnhastral.registry.sound.CASoundEvents.AMBIENT_ASTRAL;

public class CAMusics {

    public static final Music ASTRAL_BGM = Musics
            .createGameMusic(AMBIENT_ASTRAL.getHolder().orElseThrow());
}
