package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mekeng.github.common.me.inventory.IGasInventoryHost;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.tile.base.MEGasBus;

import com.mmcefixes.polling.MEGasInventory;


@Mixin(value = MEGasBus.class, remap = false)
public abstract class MixinMEGasBusInventory {

    @Redirect(method = "<init>", at = @At(value = "NEW",
        target = "com/mekeng/github/common/me/inventory/impl/GasInventory"))
    private GasInventory mmcefixes$createTanks(int slots, int capacity, IGasInventoryHost owner) {
        return new MEGasInventory((MEGasBus) owner, slots, capacity);
    }
}
