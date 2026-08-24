package com.ctnh.ctnhastral.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class EnglishLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("enchantment.ctnhastral.vacuum_seal.desc",
                "Protects you from vacuum damage. Note: All equipped items must have this enchantment to take effect");
        provider.add("celestial_body.ctnhastral.sol", "Sol");
        provider.add("celestial_body.ctnhastral.sol.desc", "The star at the center of the solar system");
        provider.add("celestial_body.ctnhastral.earth", "Earth");
        provider.add("celestial_body.ctnhastral.earth.desc", "A homeworld with a breathable atmosphere");
        provider.add("celestial_body.ctnhastral.moon", "Moon");
        provider.add("celestial_body.ctnhastral.moon.desc", "A rocky satellite without natural oxygen");
        provider.add("celestial_body.ctnhastral.mars", "Mars");
        provider.add("celestial_body.ctnhastral.mars.desc", "A cold, thin-aired red planet");
        provider.add("celestial_body.ctnhastral.venus", "Venus");
        provider.add("celestial_body.ctnhastral.venus.desc", "A hot, high-pressure planet");
        provider.add("celestial_body.ctnhastral.astral_planet", "Astral Planet");
        provider.add("celestial_body.ctnhastral.astral_planet.desc",
                "An astral world far beyond the solar system");
        provider.add("celestial_body.ctnhastral.astral_orbit", "Astral Orbit");
        provider.add("celestial_body.ctnhastral.astral_orbit.desc",
                "Orbital space above the astral planet");
        provider.add("gui.ctnhastral.celestial_map", "Celestial Map");
        provider.add("gui.ctnhastral.celestial_map.origin", "Current location: %s");
        provider.add("gui.ctnhastral.celestial_map.origin_unknown", "Current location: Unknown");
        provider.add("gui.ctnhastral.celestial_map.select", "Select a reachable body");
        provider.add("gui.ctnhastral.celestial_map.tier", "Required rocket tier: %s");
        provider.add("message.ctnhastral.invalid_destination", "The destination is invalid or the map expired");
        provider.add("message.ctnhastral.rocket_tier_low", "Rocket tier too low: %s required, %s available");
    }
}
