package com.mmcefixes.network;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

import com.mmcefixes.polling.ContainerMEBusPollingRate;


public class PacketSetPollingRate implements IMessage, IMessageHandler<PacketSetPollingRate, IMessage> {
    private int rate;
    private int windowId;

    public PacketSetPollingRate() {}

    public PacketSetPollingRate(int rate, int windowId) {
        this.rate = rate;
        this.windowId = windowId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        rate = buf.readInt();
        windowId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(rate);
        buf.writeInt(windowId);
    }

    @Override
    public IMessage onMessage(PacketSetPollingRate message, MessageContext context) {
        EntityPlayerMP player = context.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (player.openContainer.windowId != message.windowId) return;
            if (!(player.openContainer instanceof ContainerMEBusPollingRate)) return;

            ContainerMEBusPollingRate container = (ContainerMEBusPollingRate) player.openContainer;
            if (container.canEdit(player)) container.setPollingRate(Math.max(0, message.rate));
        });

        return null;
    }
}
