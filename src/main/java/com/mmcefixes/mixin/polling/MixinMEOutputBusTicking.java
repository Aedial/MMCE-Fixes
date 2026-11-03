package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import github.kasuminova.mmce.common.tile.MEFluidOutputBus;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;

import com.mmcefixes.duck.PollingBus;
import com.mmcefixes.polling.MEBusPolling;


@Mixin(value = {MEItemOutputBus.class, MEFluidOutputBus.class}, remap = false)
public abstract class MixinMEOutputBusTicking extends MEMachineComponent {

    @Inject(method = "tickingRequest", at = @At("RETURN"), cancellable = true)
    private void mmcefixes$modulate(IGridNode node, int ticks, CallbackInfoReturnable<TickRateModulation> cir) {
        MEBusPolling polling = ((PollingBus) this).mmcefixes$getPolling();
        TickRateModulation result = cir.getReturnValue();
        if (result == TickRateModulation.IDLE) {
            if (!proxy.isActive()) cir.setReturnValue(polling.inactive());
            return;
        }

        cir.setReturnValue(polling.worked(result == TickRateModulation.FASTER, ticks));
    }
}
