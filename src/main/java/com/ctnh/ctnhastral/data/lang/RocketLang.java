package com.ctnh.ctnhastral.data.lang;

import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.ctnhlang.Key;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

public final class RocketLang {

    private RocketLang() {}

    @Key("message.ctnhastral.rocket.launch")
    @EN("Press %1$s to launch")
    @CN("按%1$s发射")
    public static Lang launch;

    @Key("gui.ctnhastral.rocket.clear")
    @EN("Clear Rocket")
    @CN("清除火箭")
    public static Lang clear;
}
