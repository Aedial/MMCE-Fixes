package com.mmcefixes.mixin.polling;

import java.util.concurrent.locks.ReadWriteLock;

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
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.me.GridAccessException;
import github.kasuminova.mmce.common.tile.MEFluidInputBus;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;

import com.mmcefixes.duck.PollingBus;
import com.mmcefixes.polling.MEFluidConfigInventory;


@Mixin(value = MEFluidInputBus.class, remap = false)
public abstract class MixinMEFluidInputBus extends MEFluidBus implements PollingBus {

    @Shadow @Final private AEFluidInventory config;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "appeng/fluids/util/AEFluidInventory"))
    private AEFluidInventory mmcefixes$createFilters(IAEFluidInventory owner, int slots) {
        return new MEFluidConfigInventory((MEFluidInputBus) owner, slots);
    }

    @Shadow
    private boolean needsUpdate() {
        throw new AssertionError();
    }

    @Shadow
    private IAEFluidStack extractStackFromAE(IMEMonitor<IAEFluidStack> inventory, IAEFluidStack stack)
            throws GridAccessException {
        throw new AssertionError();
    }

    @Shadow
    private IAEFluidStack insertStackToAE(IMEMonitor<IAEFluidStack> inventory, IAEFluidStack stack)
            throws GridAccessException {
        throw new AssertionError();
    }

    @Override
    public boolean mmcefixes$hasWork() {
        return needsUpdate();
    }

    @Override
    public GuiType mmcefixes$getMainGui() {
        return GuiType.ME_FLUID_INPUT_BUS;
    }

    @Inject(method = "getTickingRequest", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$tickingRequest(IGridNode node, CallbackInfoReturnable<TickingRequest> cir) {
        cir.setReturnValue(mmcefixes$getPolling().getTickingRequest());
    }

    @Overwrite
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) return mmcefixes$getPolling().inactive();

        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) {
            return mmcefixes$getPolling().noWork(ticksSinceLastCall);
        }

        ReadWriteLock rwLock = tanks.getRWLock();

        try {
            rwLock.writeLock().lock();

            boolean successAtLeastOnce = false;
            inTick = true;
            IMEMonitor<IAEFluidStack> inv = proxy.getStorage().getInventory(channel);
            int capacity = tanks.getCapacity();
            for (final int slot : needUpdateSlots) {
                changedSlots[slot] = false;
                IAEFluidStack cfgStack = config.getFluidInSlot(slot);
                IAEFluidStack invStack = tanks.getFluidInSlot(slot);

                if (cfgStack == null) {
                    if (invStack == null) continue;
                    tanks.setFluidInSlot(slot, insertStackToAE(inv, invStack));
                    continue;
                }

                if (!cfgStack.equals(invStack)) {
                    if (invStack != null) {
                        IAEFluidStack stack = insertStackToAE(inv, invStack);
                        tanks.setFluidInSlot(slot, stack);
                        if (stack != null) continue;
                    }

                    IAEFluidStack stack = extractStackFromAE(inv, cfgStack.copy().setStackSize(capacity));
                    tanks.setFluidInSlot(slot, stack);
                    if (stack != null) successAtLeastOnce = true;

                    continue;
                }

                if (capacity == invStack.getStackSize()) continue;

                if (capacity > invStack.getStackSize()) {
                    int countToReceive = (int) (capacity - invStack.getStackSize());

                    IAEFluidStack stack = extractStackFromAE(inv, invStack.copy().setStackSize(countToReceive));
                    if (stack != null) {
                        long amount = invStack.getStackSize() + stack.getStackSize();
                        tanks.setFluidInSlot(slot, invStack.copy().setStackSize(amount));
                        successAtLeastOnce = true;
                    }

                    continue;
                }

                int countToExtract = (int) (invStack.getStackSize() - capacity);
                IAEFluidStack stack = insertStackToAE(inv, invStack.copy().setStackSize(countToExtract));
                if (stack == null) {
                    IAEFluidStack left = invStack.copy().setStackSize(invStack.getStackSize() - countToExtract);
                    tanks.setFluidInSlot(slot, left);
                } else {
                    long amount = invStack.getStackSize() - countToExtract + stack.getStackSize();
                    tanks.setFluidInSlot(slot, invStack.copy().setStackSize(amount));
                }
                successAtLeastOnce = true;
            }

            inTick = false;
            rwLock.writeLock().unlock();
            return mmcefixes$getPolling().worked(successAtLeastOnce, ticksSinceLastCall);
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[TANK_SLOT_AMOUNT];
            rwLock.writeLock().unlock();
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
        config.readFromNBT(tag, "config");
        mmcefixes$getPolling().readSettings(tag);
        markForUpdate();
        ci.cancel();
    }
}
