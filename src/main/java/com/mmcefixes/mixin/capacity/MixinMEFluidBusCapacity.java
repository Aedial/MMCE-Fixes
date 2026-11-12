package com.mmcefixes.mixin.capacity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.config.Upgrades;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import github.kasuminova.mmce.common.util.AEFluidInventoryUpgradeable;

import com.mmcefixes.capacity.TankCapacity;
import com.mmcefixes.config.MMCEFixesCommonConfig;


@Mixin(value = MEFluidBus.class, remap = false)
public abstract class MixinMEFluidBusCapacity {
    @Shadow @Final protected AEFluidInventoryUpgradeable tanks;

    @Shadow public abstract int getInstalledUpgrades(Upgrades upgrade);

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 8000))
    private int mmcefixes$baseCapacity(int original) {
        return Math.max(1, MMCEFixesCommonConfig.general.meFluidBusBaseCapacity);
    }

    @Inject(method = "updateTankCapacity", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$updateCapacity(CallbackInfo ci) {
        tanks.setCapacity(TankCapacity.calculate(
            MMCEFixesCommonConfig.general.meFluidBusBaseCapacity, getInstalledUpgrades(Upgrades.CAPACITY)));
        ci.cancel();
    }
}
