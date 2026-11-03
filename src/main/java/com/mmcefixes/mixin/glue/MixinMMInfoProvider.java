package com.mmcefixes.mixin.glue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import hellfirepvp.modularmachinery.common.integration.theoneprobe.MMInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import com.mmcefixes.duck.GluedBus;


@Mixin(value = MMInfoProvider.class, remap = false)
public abstract class MixinMMInfoProvider {

    @Inject(method = "addProbeInfo", at = @At("RETURN"))
    private void mmcefixes$showGlue(ProbeMode mode, IProbeInfo info, EntityPlayer player,
            World world, IBlockState state, IProbeHitData hit, CallbackInfo ci) {
        TileEntity tile = world.getTileEntity(hit.getPos());
        if (tile instanceof GluedBus && ((GluedBus) tile).mmcefixes$isGlued()) {
            info.text(TextFormatting.AQUA + "{*mmcefixes.glue.top*}");
        }
    }
}
