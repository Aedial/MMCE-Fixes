package com.mmcefixes.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Loader;

import zone.rong.mixinbooter.ILateMixinLoader;

import com.mmcefixes.config.MMCEFixesConfig;


public class MMCEFixesLateMixinLoader implements ILateMixinLoader {
    private static final String PREFIX = "mixins.mmcefixes.";

    @Override
    public List<String> getMixinConfigs() {
        MMCEFixesConfig.load();

        List<String> configs = new ArrayList<>();
        if (MMCEFixesConfig.mixins.gluedHatches) {
            configs.add(PREFIX + "201.json");
            if (Loader.isModLoaded("theoneprobe")) configs.add(PREFIX + "201.top.json");
        }

        if (!Loader.isModLoaded("appliedenergistics2")) return configs;

        boolean gas = Loader.isModLoaded("mekeng") && Loader.isModLoaded("mekanism");
        if (MMCEFixesConfig.mixins.meFluidSize) {
            configs.add(PREFIX + "200.json");
            if (gas) configs.add(PREFIX + "200.gas.json");
        }

        if (MMCEFixesConfig.mixins.mePolling) {
            configs.add(PREFIX + "199.json");
            if (gas) configs.add(PREFIX + "199.gas.json");
        }

        return configs;
    }
}
