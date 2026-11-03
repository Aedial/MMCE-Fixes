package com.mmcefixes.polling;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.MEFluidInputBus;


public class MEFluidConfigInventory extends AEFluidInventory {
    private final MEFluidInputBus owner;

    public MEFluidConfigInventory(MEFluidInputBus owner, int slots) {
        super(owner, slots);
        this.owner = owner;
    }

    @Override
    public void setFluidInSlot(int slot, IAEFluidStack fluid) {
        super.setFluidInSlot(slot, fluid);
        if (!Platform.isServer() && owner.hasWorld() && !owner.getWorld().isRemote) {
            owner.onFluidInventoryChanged(this, slot);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, String name) {
        NBTTagCompound contents = tag.getCompoundTag(name);
        for (int slot = 0; slot < getSlots(); slot++) {
            setFluidInSlot(slot, AEFluidStack.fromNBT(contents.getCompoundTag("#" + slot)));
        }
    }
}
