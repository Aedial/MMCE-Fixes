package com.mmcefixes.mixin.polling;

import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import github.kasuminova.mmce.common.tile.MEItemInputBus;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.util.ItemUtils;

import com.mmcefixes.duck.PollingBus;


@Mixin(value = MEItemInputBus.class, remap = false)
public abstract class MixinMEItemInputBus extends MEItemBus implements PollingBus {

    @Shadow private IOInventory configInventory;

    @Shadow public abstract void readConfigInventoryNBT(NBTTagCompound tag);

    @Shadow
    private boolean needsUpdate() {
        throw new AssertionError();
    }

    @Shadow
    private ItemStack extractStackFromAE(IMEMonitor<IAEItemStack> inventory, ItemStack stack)
            throws GridAccessException {
        throw new AssertionError();
    }

    @Shadow
    private ItemStack insertStackToAE(IMEMonitor<IAEItemStack> inventory, ItemStack stack)
            throws GridAccessException {
        throw new AssertionError();
    }

    @Override
    public boolean mmcefixes$hasWork() {
        return needsUpdate();
    }

    @Override
    public GuiType mmcefixes$getMainGui() {
        return GuiType.ME_ITEM_INPUT_BUS;
    }

    @Inject(method = "getTickingRequest", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$tickingRequest(IGridNode node, CallbackInfoReturnable<TickingRequest> cir) {
        cir.setReturnValue(mmcefixes$getPolling().getTickingRequest());
    }

    /**
     * @author Aedial
     * @reason Apply the polling branch's transfer loop and returned resource remainders
     */
    @Overwrite
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return mmcefixes$getPolling().inactive();
        }

        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) {
            return mmcefixes$getPolling().noWork(ticksSinceLastCall);
        }

        ReadWriteLock rwLock = inventory.getRWLock();

        try {
            rwLock.writeLock().lock();

            boolean successAtLeastOnce = false;
            inTick = true;
            IMEMonitor<IAEItemStack> inv = proxy.getStorage().getInventory(channel);
            for (final int slot : needUpdateSlots) {
                changedSlots[slot] = false;
                ItemStack cfgStack = configInventory.getStackInSlot(slot);
                ItemStack invStack = inventory.getStackInSlot(slot);

                if (cfgStack.isEmpty()) {
                    if (invStack.isEmpty()) {
                        continue;
                    }
                    inventory.setStackInSlot(slot, insertStackToAE(inv, invStack));
                    continue;
                }

                if (!ItemUtils.matchStacks(cfgStack, invStack)) {
                    if (!invStack.isEmpty()) {
                        ItemStack left = insertStackToAE(inv, invStack);
                        inventory.setStackInSlot(slot, left);
                        if (!left.isEmpty()) {
                            continue;
                        }
                    }

                    ItemStack stack = extractStackFromAE(inv, cfgStack);
                    inventory.setStackInSlot(slot, stack);
                    if (!stack.isEmpty()) {
                        successAtLeastOnce = true;
                    }
                    continue;
                }

                if (cfgStack.getCount() == invStack.getCount()) {
                    continue;
                }

                if (cfgStack.getCount() > invStack.getCount()) {
                    int countToReceive = cfgStack.getCount() - invStack.getCount();
                    ItemStack stack = extractStackFromAE(inv, ItemUtils.copyStackWithSize(invStack, countToReceive));
                    if (!stack.isEmpty()) {
                        int newCount = invStack.getCount() + stack.getCount();
                        inventory.setStackInSlot(slot, ItemUtils.copyStackWithSize(invStack, newCount));
                        successAtLeastOnce = true;
                        failureCounter[slot] = 0;
                    } else {
                        // If AE doesn't have enough item?
                        failureCounter[slot]++;
                    }
                } else {
                    int countToExtract = invStack.getCount() - cfgStack.getCount();
                    ItemStack stack = insertStackToAE(inv, ItemUtils.copyStackWithSize(invStack, countToExtract));
                    if (stack.isEmpty()) {
                        inventory.setStackInSlot(slot, ItemUtils.copyStackWithSize(
                            invStack, invStack.getCount() - countToExtract)
                        );
                    } else {
                        inventory.setStackInSlot(slot, ItemUtils.copyStackWithSize(
                            invStack, invStack.getCount() - countToExtract + stack.getCount())
                        );
                    }
                    successAtLeastOnce = true;
                }
            }

            inTick = false;
            rwLock.writeLock().unlock();
            return mmcefixes$getPolling().worked(successAtLeastOnce, ticksSinceLastCall);
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[changedSlots.length];
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
        readConfigInventoryNBT(tag.getCompoundTag("configInventory"));
        mmcefixes$getPolling().readSettings(tag);
        markForUpdate();
        ci.cancel();
    }
}
