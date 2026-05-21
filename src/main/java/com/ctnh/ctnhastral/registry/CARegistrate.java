package com.ctnh.ctnhastral.registry;

import com.ctnh.ctnhastral.CTNHAstral;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public class CARegistrate extends CNRegistrate {

    protected CARegistrate() {
        super(CTNHAstral.MODID);
    }

    public static CARegistrate create() {
        return new CARegistrate();
    }
}
