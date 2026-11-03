package com.mmcefixes.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import com.mmcefixes.Tags;


public final class FixesNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    private FixesNetwork() {}

    public static void init() {
        int id = 0;

        CHANNEL.registerMessage(PacketOpenPollingGui.class, PacketOpenPollingGui.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketSetPollingRate.class, PacketSetPollingRate.class, id++, Side.SERVER);
    }
}
