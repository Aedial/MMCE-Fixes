package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickingRequest;
import github.kasuminova.mmce.common.tile.MEFluidOutputBus;
import github.kasuminova.mmce.common.tile.SettingsTransfer;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;

import com.mmcefixes.duck.PollingBus;


@Mixin(value = MEFluidOutputBus.class, remap = false)
public abstract class MixinMEFluidOutputBus extends MEFluidBus implements PollingBus, SettingsTransfer {

    @Shadow public abstract boolean hasFluid();

    @Override
    public boolean mmcefixes$hasWork() {
        return hasFluid();
    }

    @Override
    public GuiType mmcefixes$getMainGui() {
        return GuiType.ME_FLUID_OUTPUT_BUS;
    }

    @Inject(method = "getTickingRequest", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$tickingRequest(IGridNode node, CallbackInfoReturnable<TickingRequest> cir) {
        cir.setReturnValue(mmcefixes$getPolling().getTickingRequest());
    }

    @Inject(method = "markNoUpdate", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$markChanged(CallbackInfo ci) {
        super.markNoUpdate();
        ci.cancel();
    }

    @Override
    public NBTTagCompound downloadSettings() {
        NBTTagCompound tag = new NBTTagCompound();
        mmcefixes$getPolling().write(tag);
        return tag;
    }

    @Override
    public void uploadSettings(NBTTagCompound tag) {
        mmcefixes$getPolling().readSettings(tag);
    }
}
