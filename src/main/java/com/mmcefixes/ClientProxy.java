package com.mmcefixes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.IGuiHandler;

import com.mmcefixes.polling.ClientPollingGuiHandler;
import com.mmcefixes.polling.PollingGuiEvents;


public class ClientProxy extends CommonProxy {

    @Override
    protected IGuiHandler createPollingGuiHandler() {
        MinecraftForge.EVENT_BUS.register(PollingGuiEvents.class);
        return new ClientPollingGuiHandler();
    }
}
