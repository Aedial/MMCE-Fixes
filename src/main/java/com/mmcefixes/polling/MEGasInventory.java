package com.mmcefixes.polling;

import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.tile.base.MEGasBus;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;


/**
 * Gas inventory for ME buses with callbacks based on the owner's world.
 */
public class MEGasInventory extends GasInventory {

    private final MEGasBus owner;

    public MEGasInventory(final MEGasBus owner, final int slots, final int capacity) {
        super(slots, capacity);
        this.owner = owner;

        // Use these tanks for inherited gas operations and NBT loading
        GasTank[] tanks = getTanks();
        for (int slot = 0; slot < tanks.length; slot++) {
            tanks[slot] = new NotifiableGasTank(slot, capacity);
        }
    }

    public MEGasInventory(final MEGasBus owner, final int slots) {
        this(owner, slots, Integer.MAX_VALUE);
    }

    private class NotifiableGasTank extends GasTank {

        private final int slot;

        private NotifiableGasTank(final int slot, final int capacity) {
            super(capacity);
            this.slot = slot;
        }

        @Override
        public GasStack draw(final int amount, final boolean doDraw) {
            GasStack drawn = super.draw(amount, doDraw);
            if (doDraw) onContentChanged();

            return drawn;
        }

        @Override
        public int receive(final GasStack stack, final boolean doReceive) {
            int received = super.receive(stack, doReceive);
            if (doReceive) onContentChanged();

            return received;
        }

        @Override
        public void setMaxGas(final int capacity) {
            super.setMaxGas(capacity);
            onContentChanged();
        }

        @Override
        public void setGas(final GasStack stack) {
            // Retain gas above the current capacity
            this.stored = stack != null && stack.amount > 0 ? stack : null;
            onContentChanged();
        }

        private void onContentChanged() {
            if (owner.hasWorld() && !owner.getWorld().isRemote) {
                owner.onGasInventoryChanged(MEGasInventory.this, this.slot);
            }
        }
    }
}

