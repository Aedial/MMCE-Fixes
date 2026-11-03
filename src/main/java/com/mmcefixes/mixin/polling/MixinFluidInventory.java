package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import github.kasuminova.mmce.common.util.AEFluidInventoryUpgradeable;

import com.mmcefixes.duck.PollingBus;


@Mixin(value = AEFluidInventoryUpgradeable.class, remap = false)
public abstract class MixinFluidInventory {
    @Shadow @Final private IAEFluidInventory handler;

    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$notifyOwner(int slot, CallbackInfo ci) {
        if (!(handler instanceof PollingBus)) return;

        TileEntity owner = (TileEntity) handler;
        if (owner.hasWorld() && !owner.getWorld().isRemote) {
            handler.onFluidInventoryChanged((IAEFluidTank) this, slot);
        }

        ci.cancel();
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V",
        at = @At("HEAD"), cancellable = true)
    private void mmcefixes$readTanks(NBTTagCompound tag, String name, CallbackInfo ci) {
        if (!(handler instanceof PollingBus)) return;

        IAEFluidTank tanks = (IAEFluidTank) this;
        NBTTagCompound contents = tag.getCompoundTag(name);
        for (int slot = 0; slot < tanks.getSlots(); slot++) {
            tanks.setFluidInSlot(slot, AEFluidStack.fromNBT(contents.getCompoundTag("#" + slot)));
        }

        ci.cancel();
    }
}
