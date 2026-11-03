package com.mmcefixes.mixin.capacity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.container.guisync.GuiSync;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.container.ContainerMEGasBus;
import github.kasuminova.mmce.common.tile.base.MEGasBus;


@Mixin(value = ContainerMEGasBus.class, remap = false)
public abstract class MixinContainerMEGasBus {
    @Shadow @Final private MEGasBus owner;

    // AE2 sends long fields through PacketProgressBar
    @Unique @GuiSync(8)
    public long mmcefixes$tankCapacity;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mmcefixes$initialCapacity(InventoryPlayer player, MEGasBus host, CallbackInfo ci) {
        mmcefixes$tankCapacity = host.getTanks().getTanks()[0].getMaxGas();
    }

    @Inject(method = "detectAndSendChanges", at = @At("HEAD"), remap = true)
    private void mmcefixes$syncCapacity(CallbackInfo ci) {
        if (!owner.getWorld().isRemote) mmcefixes$tankCapacity = owner.getTanks().getTanks()[0].getMaxGas();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
        target = "Lcom/mekeng/github/common/me/inventory/impl/GasInventory;setCap(I)V"))
    private void mmcefixes$awaitCapacity(GasInventory tanks, int capacity) {
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void mmcefixes$applyCapacity(String field, Object oldValue, Object newValue, CallbackInfo ci) {
        if (owner.getWorld().isRemote && field.equals("mmcefixes$tankCapacity")) {
            owner.getTanks().setCap((int) mmcefixes$tankCapacity);
        }
    }
}
