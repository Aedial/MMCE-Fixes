package com.mmcefixes;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import com.mmcefixes.config.MMCEFixesCommonConfig;
import com.mmcefixes.network.FixesNetwork;
import com.mmcefixes.polling.PollingGuiHandler;


public class CommonProxy {

    public void preInit() {
        if (!MMCEFixesCommonConfig.mixins.mePolling || !Loader.isModLoaded("appliedenergistics2")) return;

        FixesNetwork.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(MMCEFixes.instance, createPollingGuiHandler());
    }

    protected IGuiHandler createPollingGuiHandler() {
        return new PollingGuiHandler();
    }
}
