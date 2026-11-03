package com.mmcefixes.polling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import github.kasuminova.mmce.common.tile.base.MEMachineComponent;

import com.mmcefixes.duck.PollingBus;


public class ClientPollingGuiHandler extends PollingGuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id != 0 || !(tile instanceof PollingBus)) return null;

        return new GuiMEBusPollingRate(player.inventory, (MEMachineComponent) tile);
    }
}
