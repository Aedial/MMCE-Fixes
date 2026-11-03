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
import github.kasuminova.mmce.common.container.ContainerMEFluidBus;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import github.kasuminova.mmce.common.util.AEFluidInventoryUpgradeable;


@Mixin(value = ContainerMEFluidBus.class, remap = false)
public abstract class MixinContainerMEFluidBus {
    @Shadow @Final private MEFluidBus owner;

    // AE2 sends long fields through PacketProgressBar
    @Unique @GuiSync(8)
    public long mmcefixes$tankCapacity;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mmcefixes$initialCapacity(InventoryPlayer player, MEFluidBus host, CallbackInfo ci) {
        mmcefixes$tankCapacity = ((AEFluidInventoryUpgradeable) host.getTanks()).getCapacity();
    }

    @Inject(method = "detectAndSendChanges", at = @At("HEAD"), remap = true)
    private void mmcefixes$syncCapacity(CallbackInfo ci) {
        if (!owner.getWorld().isRemote) {
            mmcefixes$tankCapacity = ((AEFluidInventoryUpgradeable) owner.getTanks()).getCapacity();
        }
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
        target = "Lgithub/kasuminova/mmce/common/util/AEFluidInventoryUpgradeable;setCapacity(I)V"))
    private void mmcefixes$awaitCapacity(AEFluidInventoryUpgradeable tanks, int capacity) {
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void mmcefixes$applyCapacity(String field, Object oldValue, Object newValue, CallbackInfo ci) {
        if (owner.getWorld().isRemote && field.equals("mmcefixes$tankCapacity")) {
            ((AEFluidInventoryUpgradeable) owner.getTanks()).setCapacity((int) mmcefixes$tankCapacity);
        }
    }
}
