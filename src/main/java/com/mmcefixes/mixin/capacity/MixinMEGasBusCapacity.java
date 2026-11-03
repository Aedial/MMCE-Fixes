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
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.tile.base.MEGasBus;

import com.mmcefixes.capacity.TankCapacity;
import com.mmcefixes.config.MMCEFixesConfig;


@Mixin(value = MEGasBus.class, remap = false)
public abstract class MixinMEGasBusCapacity {
    @Shadow @Final protected GasInventory tanks;

    @Shadow public abstract int getInstalledUpgrades(Upgrades upgrade);

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 8000))
    private int mmcefixes$baseCapacity(int original) {
        return Math.max(1, MMCEFixesConfig.general.meGasBusBaseCapacity);
    }

    @Inject(method = "updateTankCapacity", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$updateCapacity(CallbackInfo ci) {
        tanks.setCap(TankCapacity.calculate(
            MMCEFixesConfig.general.meGasBusBaseCapacity, getInstalledUpgrades(Upgrades.CAPACITY)));
        ci.cancel();
    }
}
