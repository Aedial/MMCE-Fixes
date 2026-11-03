package com.mmcefixes.polling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import github.kasuminova.mmce.common.integration.ModIntegrationAE2;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;

import com.mmcefixes.duck.PollingBus;


public class PollingGuiHandler implements IGuiHandler {

    public static boolean canEdit(EntityPlayer player, MEMachineComponent host) {
        return host.hasWorld() && !host.isInvalid() && host.getWorld() == player.world
            && player.getDistanceSq(host.getPos()) <= 64.0D
            && !ModIntegrationAE2.securityCheck(player, host.getProxy());
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != 0 || !world.isBlockLoaded(new BlockPos(x, y, z))) return null;

        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (!(tile instanceof PollingBus)) return null;

        MEMachineComponent host = (MEMachineComponent) tile;
        return canEdit(player, host) ? new ContainerMEBusPollingRate(player.inventory, host) : null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}
