package com.mmcefixes.mixin.polling;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import github.kasuminova.mmce.common.tile.MEGasOutputBus;
import github.kasuminova.mmce.common.tile.SettingsTransfer;
import github.kasuminova.mmce.common.tile.base.MEGasBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import mekanism.api.gas.GasStack;

import com.mmcefixes.duck.PollingBus;


@Mixin(value = MEGasOutputBus.class, remap = false)
public abstract class MixinMEGasOutputBus extends MEGasBus implements PollingBus, SettingsTransfer {

    @Shadow public abstract boolean hasGas();

    @Override
    public boolean mmcefixes$hasWork() {
        return hasGas();
    }

    @Override
    public GuiType mmcefixes$getMainGui() {
        return GuiType.ME_GAS_OUTPUT_BUS;
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
        boolean successAtLeastOnce = false;

        try {
            IMEMonitor<IAEGasStack> inv = proxy.getStorage().getInventory(channel);
            synchronized (tanks) {
                for (final int slot : needUpdateSlots) {
                    changedSlots[slot] = false;
                    GasStack gas = tanks.getGasStack(slot);
                    if (gas == null) continue;

                    IAEGasStack stack = Objects.requireNonNull(AEGasStack.of(gas));
                    IAEGasStack left = Platform.poweredInsert(proxy.getEnergy(), inv, stack, source);

                    if (left != null) {
                        if (gas.amount != left.getStackSize()) successAtLeastOnce = true;
                    } else {
                        successAtLeastOnce = true;
                    }

                    tanks.setGas(slot, left == null ? null : left.getGasStack());
                }
            }
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[TANK_SLOT_AMOUNT];
            return TickRateModulation.IDLE;
        }

        inTick = false;
        return mmcefixes$getPolling().worked(successAtLeastOnce, ticksSinceLastCall);
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
