package com.mmcefixes.mixin.glue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NBTTagCompound;

import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;

import com.mmcefixes.config.MMCEFixesConfig;
import com.mmcefixes.duck.GluedBus;
import com.mmcefixes.glue.GluedHatches;


@Mixin(value = TileItemBus.class, remap = false)
public abstract class MixinTileItemBus extends TileInventory implements GluedBus {
    @Shadow protected int successCounter;
    @Shadow protected boolean inventoryChanged;

    @Unique private boolean mmcefixes$glued =
        GluedHatches.isSupportedTile(this) && MMCEFixesConfig.general.gluedByDefault;

    @Override
    public boolean mmcefixes$isGlued() {
        return mmcefixes$glued && GluedHatches.isSupportedBlock(getBlockType());
    }

    @Override
    public boolean mmcefixes$setGlued(boolean glued) {
        if (!GluedHatches.isSupportedTile(this)) return false;

        if (mmcefixes$glued == glued) return false;

        mmcefixes$glued = glued;
        successCounter = 0;
        inventoryChanged = true;

        if (hasWorld() && !world.isRemote) markForUpdate();

        return true;
    }

    @Inject(method = "canWork", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$stopAutomaticIO(int min, int max, CallbackInfoReturnable<Boolean> cir) {
        if (mmcefixes$isGlued()) cir.setReturnValue(false);
    }

    @Inject(method = "readCustomNBT", at = @At("RETURN"))
    private void mmcefixes$readGlue(NBTTagCompound tag, CallbackInfo ci) {
        if (!GluedHatches.isSupportedTile(this)) return;

        boolean wasGlued = mmcefixes$glued;
        mmcefixes$glued = tag.getBoolean("disableExternalIO");
        if (wasGlued != mmcefixes$glued && hasWorld() && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Inject(method = "writeCustomNBT", at = @At("RETURN"))
    private void mmcefixes$writeGlue(NBTTagCompound tag, CallbackInfo ci) {
        if (GluedHatches.isSupportedTile(this)) tag.setBoolean("disableExternalIO", mmcefixes$glued);
    }
}
