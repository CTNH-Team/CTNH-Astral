package com.ctnh.ctnhrogue.registry;

import com.ctnh.ctnhrogue.CTNHRogue;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public class CRRegistrate extends CNRegistrate {

    protected CRRegistrate() {
        super(CTNHRogue.MODID);
    }

    public static CRRegistrate create() {
        return new CRRegistrate();
    }
}
