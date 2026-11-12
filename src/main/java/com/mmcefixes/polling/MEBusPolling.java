package com.mmcefixes.polling;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.GridAccessException;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;
import hellfirepvp.modularmachinery.ModularMachinery;

import com.mmcefixes.config.MMCEFixesCommonConfig;
import com.mmcefixes.duck.PollingBus;


public final class MEBusPolling {
    private final MEMachineComponent host;
    private int rate = Math.max(0, MMCEFixesCommonConfig.general.defaultPollingRate);
    private boolean sleeping;
    private boolean pendingWakeUp;
    private boolean loggedWakeUpFailure;

    public MEBusPolling(MEMachineComponent host) {
        this.host = host;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int ticks) {
        int sanitized = Math.max(0, ticks);
        if (rate == sanitized) return;

        rate = sanitized;
        sleeping = false;
        host.markForUpdate();

        if (host.getProxy().isReady()) {
            TickManagerHelper.reRegisterTickable(host.getProxy().getNode(), (IGridTickable) host);
        }
    }

    public TickingRequest getTickingRequest() {
        if (rate > 0) {
            sleeping = false;
            return new TickingRequest(rate, rate, false, true);
        }

        int min = Math.max(1, MMCEFixesCommonConfig.general.adaptivePollingMin);
        int max = Math.max(min, MMCEFixesCommonConfig.general.adaptivePollingMax);
        sleeping = !MMCEFixesCommonConfig.general.adaptivePollingWaitForSleep && !hasWork();
        return new TickingRequest(min, max, sleeping, true);
    }

    public TickRateModulation inactive() {
        sleeping = false;
        return rate == 0 ? TickRateModulation.SLOWER : TickRateModulation.IDLE;
    }

    public TickRateModulation noWork(int ticksSinceLastCall) {
        sleeping = false;
        if (rate > 0) return TickRateModulation.SAME;
        if (hasWork()) return TickRateModulation.SLOWER;

        // Only sleep if we're at max delay and still have no work to do. This prevents the flow of :
        // 1. Work to do, wake up
        // 2. No work to do, sleep (after 5 ticks)
        // 3. Work to do, wake up immediately
        // Which would cause constant sleeping and waking if the speed is at the right interval,
        // negating the optimization that adaptive polling is supposed to provide
        // We only wake up on recipe completion if we're sleeping
        int max = Math.max(1, Math.max(MMCEFixesCommonConfig.general.adaptivePollingMin,
            MMCEFixesCommonConfig.general.adaptivePollingMax));
        if (!MMCEFixesCommonConfig.general.adaptivePollingWaitForSleep || ticksSinceLastCall >= max) {
            sleeping = true;
            return TickRateModulation.SLEEP;
        }

        return TickRateModulation.SLOWER;
    }

    public TickRateModulation worked(boolean didWork, int ticksSinceLastCall) {
        if (rate > 0) {
            sleeping = false;
            return TickRateModulation.SAME;
        }

        if (didWork) {
            sleeping = false;
            return TickRateModulation.FASTER;
        }

        return noWork(ticksSinceLastCall);
    }

    public void markChanged() {
        if (pendingWakeUp || (rate == 0 && sleeping && hasWork())) wakeIfNeeded();
    }

    public void gridChanged() {
        if (pendingWakeUp) wakeIfNeeded();
    }

    public void queueWakeUp() {
        if (!host.hasWorld() || host.getWorld().isRemote) return;

        // Check work after subclass inventories and filters finish loading
        pendingWakeUp = true;
        host.markNoUpdateSync();
    }

    private boolean hasWork() {
        return ((PollingBus) host).mmcefixes$hasWork();
    }

    private void wakeIfNeeded() {
        if (!host.hasWorld() || host.getWorld().isRemote) return;

        if (!hasWork()) {
            pendingWakeUp = false;
            loggedWakeUpFailure = false;
            return;
        }

        try {
            IGridNode node = host.getProxy().getNode();
            if (node == null) {
                deferWakeUp("the AE node is unavailable");
                return;
            }

            ITickManager manager = host.getProxy().getTick();
            if (manager.alertDevice(node) || manager.wakeDevice(node)) {
                pendingWakeUp = false;
                loggedWakeUpFailure = false;
                sleeping = false;
                return;
            }

            deferWakeUp("AE2 has no tick tracker for the node");
        } catch (GridAccessException e) {
            // Retry after AE2 reconnects the node to a grid
            deferWakeUp("the AE grid is unavailable");
        }
    }

    private void deferWakeUp(String reason) {
        pendingWakeUp = true;
        if (loggedWakeUpFailure) return;

        ModularMachinery.log.warn("Unable to wake {} at {} because {}; it will retry when the AE grid changes.",
            host.getClass().getSimpleName(), host.getPos(), reason);
        loggedWakeUpFailure = true;
    }

    public void write(NBTTagCompound tag) {
        tag.setInteger("pollingRate", rate);
    }

    public void read(NBTTagCompound tag) {
        if (tag.hasKey("pollingRate")) rate = Math.max(0, tag.getInteger("pollingRate"));
    }

    public void readSettings(NBTTagCompound tag) {
        if (tag.hasKey("pollingRate")) setRate(tag.getInteger("pollingRate"));
    }
}
