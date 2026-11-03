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
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;

import com.mmcefixes.duck.PollingBus;


@Mixin(value = MEItemOutputBus.class, remap = false)
public abstract class MixinMEItemOutputBus extends MEItemBus implements PollingBus {
    @Shadow public abstract void setConfiguredStackSize(int size);

    @Override
    public boolean mmcefixes$hasWork() {
        return hasItem();
    }

    @Override
    public GuiType mmcefixes$getMainGui() {
        return GuiType.ME_ITEM_OUTPUT_BUS;
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

    @Inject(method = "downloadSettings", at = @At("RETURN"))
    private void mmcefixes$downloadPolling(CallbackInfoReturnable<NBTTagCompound> cir) {
        mmcefixes$getPolling().write(cir.getReturnValue());
    }

    @Inject(method = "uploadSettings", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$uploadPolling(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey("configuredStackSize")) setConfiguredStackSize(tag.getInteger("configuredStackSize"));

        mmcefixes$getPolling().readSettings(tag);
        ci.cancel();
    }
}
