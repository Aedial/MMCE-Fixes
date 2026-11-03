package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.implementations.items.IMemoryCard;
import github.kasuminova.mmce.common.block.appeng.BlockMEFluidOutputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEMachineComponent;
import github.kasuminova.mmce.common.tile.SettingsTransfer;


@Mixin(BlockMEFluidOutputBus.class)
public abstract class MixinMEOutputBusBlock extends BlockMEMachineComponent {

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true, remap = true)
    private void mmcefixes$transferSettings(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
            float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (world.isRemote) return;

        TileEntity tile = world.getTileEntity(pos);
        ItemStack held = player.getHeldItem(hand);
        if (tile instanceof SettingsTransfer && held.getItem() instanceof IMemoryCard) {
            if (handleSettingsTransfer((SettingsTransfer) tile, (IMemoryCard) held.getItem(), player, held)) {
                cir.setReturnValue(true);
            }
        }
    }
}
