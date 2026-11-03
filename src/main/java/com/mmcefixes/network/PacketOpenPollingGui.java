package com.mmcefixes.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.container.AEBaseContainer;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;
import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.buffer.ByteBuf;

import com.mmcefixes.MMCEFixes;
import com.mmcefixes.duck.PollingBus;
import com.mmcefixes.polling.PollingGuiHandler;


public class PacketOpenPollingGui implements IMessage, IMessageHandler<PacketOpenPollingGui, IMessage> {
    private BlockPos pos = BlockPos.ORIGIN;
    private boolean mainGui;
    private int windowId;

    public PacketOpenPollingGui() {}

    public PacketOpenPollingGui(BlockPos pos, boolean mainGui, int windowId) {
        this.pos = pos;
        this.mainGui = mainGui;
        this.windowId = windowId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        mainGui = buf.readBoolean();
        windowId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeBoolean(mainGui);
        buf.writeInt(windowId);
    }

    @Override
    public IMessage onMessage(PacketOpenPollingGui message, MessageContext context) {
        EntityPlayerMP player = context.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (player.openContainer.windowId != message.windowId) return;
            if (!(player.openContainer instanceof AEBaseContainer)) return;
            if (!player.world.isBlockLoaded(message.pos)) return;

            TileEntity tile = player.world.getTileEntity(message.pos);
            AEBaseContainer container = (AEBaseContainer) player.openContainer;
            if (container.getTileEntity() != tile || !(tile instanceof PollingBus)) return;

            MEMachineComponent host = (MEMachineComponent) tile;
            if (!container.canInteractWith(player) || !PollingGuiHandler.canEdit(player, host)) return;

            int gui = message.mainGui ? ((PollingBus) host).mmcefixes$getMainGui().ordinal() : 0;
            Object mod = message.mainGui ? ModularMachinery.MODID : MMCEFixes.instance;
            player.openGui(mod, gui, player.world, message.pos.getX(), message.pos.getY(), message.pos.getZ());
        });

        return null;
    }
}
