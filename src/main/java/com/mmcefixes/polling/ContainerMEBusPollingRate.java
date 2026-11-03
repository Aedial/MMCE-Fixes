package com.mmcefixes.polling;

import java.util.function.IntConsumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;

import com.mmcefixes.duck.PollingBus;


public class ContainerMEBusPollingRate extends AEBaseContainer {
    private final MEMachineComponent host;
    private IntConsumer listener;

    // AE2 sends long fields through PacketProgressBar
    @GuiSync(0)
    public long pollingRate;

    public ContainerMEBusPollingRate(InventoryPlayer inventory, MEMachineComponent host) {
        super(inventory, host);
        this.host = host;
        pollingRate = ((PollingBus) host).mmcefixes$getPolling().getRate();
    }

    public void setListener(IntConsumer listener) {
        this.listener = listener;
        listener.accept((int) pollingRate);
    }

    public boolean canEdit(EntityPlayer player) {
        return canInteractWith(player) && PollingGuiHandler.canEdit(player, host);
    }

    public void setPollingRate(int rate) {
        MEBusPolling polling = ((PollingBus) host).mmcefixes$getPolling();
        polling.setRate(rate);
        pollingRate = polling.getRate();
    }

    @Override
    public void detectAndSendChanges() {
        verifyPermissions(SecurityPermissions.BUILD, false);
        if (!host.getWorld().isRemote) pollingRate = ((PollingBus) host).mmcefixes$getPolling().getRate();

        super.detectAndSendChanges();
    }

    @Override
    public void onUpdate(String field, Object oldValue, Object newValue) {
        if (field.equals("pollingRate") && listener != null) listener.accept((int) pollingRate);

        super.onUpdate(field, oldValue, newValue);
    }
}
