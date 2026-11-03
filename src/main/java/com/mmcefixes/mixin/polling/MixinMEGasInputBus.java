package com.mmcefixes.mixin.polling;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.me.GridAccessException;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventoryHost;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.tile.MEGasInputBus;
import github.kasuminova.mmce.common.tile.base.MEGasBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import mekanism.api.gas.GasStack;

import com.mmcefixes.duck.PollingBus;
import com.mmcefixes.polling.MEGasInventory;


@Mixin(value = MEGasInputBus.class, remap = false)
public abstract class MixinMEGasInputBus extends MEGasBus implements PollingBus {

    @Shadow @Final private GasInventory config;

    @Redirect(method = "<init>", at = @At(value = "NEW",
        target = "com/mekeng/github/common/me/inventory/impl/GasInventory"))
    private GasInventory mmcefixes$createFilters(int slots, IGasInventoryHost owner) {
        return new MEGasInventory((MEGasInputBus) owner, slots);
    }

    @Shadow
    private boolean needsUpdate() {
        throw new AssertionError();
    }

    @Shadow
    private IAEGasStack extractStackFromAE(IMEMonitor<IAEGasStack> inventory, GasStack stack)
            throws GridAccessException {
        throw new AssertionError();
    }

    @Shadow
    private IAEGasStack insertStackToAE(IMEMonitor<IAEGasStack> inventory, GasStack stack)
            throws GridAccessException {
        throw new AssertionError();
    }

    @Override
    public boolean mmcefixes$hasWork() {
        return needsUpdate();
    }

    @Override
    public GuiType mmcefixes$getMainGui() {
        return GuiType.ME_GAS_INPUT_BUS;
    }

    @Inject(method = "getTickingRequest", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$tickingRequest(IGridNode node, CallbackInfoReturnable<TickingRequest> cir) {
        cir.setReturnValue(mmcefixes$getPolling().getTickingRequest());
    }

    @Overwrite
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) return mmcefixes$getPolling().inactive();

        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) return mmcefixes$getPolling().noWork(ticksSinceLastCall);

        inTick = true;
        try {
            boolean successAtLeastOnce = false;

            IMEMonitor<IAEGasStack> inv = proxy.getStorage().getInventory(channel);
            int capacity = tanks.getTanks()[0].getMaxGas();

            synchronized (tanks) {
                for (final int slot : needUpdateSlots) {
                    changedSlots[slot] = false;
                    GasStack cfgStack = config.getGasStack(slot);
                    GasStack invStack = tanks.getGasStack(slot);

                    if (cfgStack == null) {
                        if (invStack == null) continue;
                        IAEGasStack left = insertStackToAE(inv, invStack);
                        tanks.setGas(slot, left == null ? null : left.getGasStack());
                        continue;
                    }

                    if (!cfgStack.isGasEqual(invStack)) {
                        if (invStack != null) {
                            IAEGasStack left = insertStackToAE(inv, invStack);
                            tanks.setGas(slot, left == null ? null : left.getGasStack());
                            if (left != null) continue;
                        }

                        GasStack copied = cfgStack.copy();
                        copied.amount = capacity;
                        IAEGasStack stack = extractStackFromAE(inv, copied);
                        tanks.setGas(slot, stack == null ? null : stack.getGasStack());
                        if (stack != null) {
                            successAtLeastOnce = true;
                        }
                        continue;
                    }

                    // Because cfgStack is not null and cfgStack.isGasEqual(invStack) is true.
                    //noinspection DataFlowIssue
                    if (capacity == invStack.amount) continue;

                    if (capacity > invStack.amount) {
                        int countToReceive = capacity - invStack.amount;

                        GasStack copied = invStack.copy();
                        copied.amount = countToReceive;
                        IAEGasStack stack = extractStackFromAE(inv, copied);
                        if (stack != null) {
                            copied = invStack.copy();
                            copied.amount = (int) (invStack.amount + stack.getStackSize());
                            tanks.setGas(slot, copied);
                            successAtLeastOnce = true;
                        }
                    } else {
                        int countToExtract = invStack.amount - capacity;
                        GasStack copied = invStack.copy();
                        copied.amount = countToExtract;
                        IAEGasStack left = insertStackToAE(inv, copied);
                        if (left == null) {
                            copied.amount = invStack.amount - countToExtract;
                            tanks.setGas(slot, copied);
                        } else {
                            copied.amount = (int) ((invStack.amount - countToExtract) + left.getStackSize());
                            tanks.setGas(slot, copied);
                        }

                        successAtLeastOnce = true;
                    }
                }
            }
            inTick = false;
            return mmcefixes$getPolling().worked(successAtLeastOnce, ticksSinceLastCall);
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[TANK_SLOT_AMOUNT];
            return TickRateModulation.IDLE;
        }
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
        config.load(tag.getCompoundTag("config"));
        mmcefixes$getPolling().readSettings(tag);
        markForUpdate();
        ci.cancel();
    }
}
