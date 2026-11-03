package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.NBTTagCompound;

import github.kasuminova.mmce.common.tile.base.MEMachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;

import com.mmcefixes.duck.PollingBus;
import com.mmcefixes.polling.MEBusPolling;


@Mixin(value = MEMachineComponent.class, remap = false)
public abstract class MixinMEMachineComponent extends TileColorableMachineComponent {
    @Unique private MEBusPolling mmcefixes$polling;

    @Unique
    public MEBusPolling mmcefixes$getPolling() {
        if (mmcefixes$polling == null) {
            mmcefixes$polling = new MEBusPolling((MEMachineComponent) (Object) this);
        }

        return mmcefixes$polling;
    }

    @Override
    public void markNoUpdate() {
        super.markNoUpdate();
        if ((Object) this instanceof PollingBus) mmcefixes$getPolling().markChanged();
    }

    @Inject(method = "gridChanged", at = @At("RETURN"))
    private void mmcefixes$gridChanged(CallbackInfo ci) {
        if ((Object) this instanceof PollingBus) mmcefixes$getPolling().gridChanged();
    }

    @Inject(method = "validate", at = @At("RETURN"), remap = true)
    private void mmcefixes$validate(CallbackInfo ci) {
        if ((Object) this instanceof PollingBus) mmcefixes$getPolling().queueWakeUp();
    }

    @Inject(method = "readCustomNBT", at = @At("RETURN"))
    private void mmcefixes$readPolling(NBTTagCompound tag, CallbackInfo ci) {
        if (!((Object) this instanceof PollingBus)) return;

        mmcefixes$getPolling().read(tag);
        mmcefixes$getPolling().queueWakeUp();
    }

    @Inject(method = "writeCustomNBT", at = @At("RETURN"))
    private void mmcefixes$writePolling(NBTTagCompound tag, CallbackInfo ci) {
        if ((Object) this instanceof PollingBus) mmcefixes$getPolling().write(tag);
    }
}
