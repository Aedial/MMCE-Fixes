package com.mmcefixes.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import zone.rong.mixinbooter.ILateMixinLoader;

import com.mmcefixes.config.MMCEFixesClientConfig;
import com.mmcefixes.config.MMCEFixesCommonConfig;


public class MMCEFixesLateMixinLoader implements ILateMixinLoader {
    private static final String PREFIX = "mixins.mmcefixes.";

    @Override
    public List<String> getMixinConfigs() {
        MMCEFixesCommonConfig.load();

        List<String> configs = new ArrayList<>();
        if (MMCEFixesCommonConfig.mixins.gluedHatches) {
            configs.add(PREFIX + "201.json");
            if (Loader.isModLoaded("theoneprobe")) configs.add(PREFIX + "201.top.json");
        }

        if (FMLCommonHandler.instance().getSide().isClient() && MMCEFixesClientConfig.mixins.clientTasks) {
            configs.add(PREFIX + "clienttasks.json");
        }

        if (!Loader.isModLoaded("appliedenergistics2")) return configs;

        boolean gas = Loader.isModLoaded("mekeng") && Loader.isModLoaded("mekanism");
        if (MMCEFixesCommonConfig.mixins.meFluidSize) {
            configs.add(PREFIX + "200.json");
            if (gas) configs.add(PREFIX + "200.gas.json");
        }

        if (MMCEFixesCommonConfig.mixins.mePolling) {
            configs.add(PREFIX + "199.json");
            if (gas) configs.add(PREFIX + "199.gas.json");
        }

        return configs;
    }
}
