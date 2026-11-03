package com.mmcefixes.polling;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.container.AEBaseContainer;
import github.kasuminova.mmce.client.gui.GuiMEFluidInputBus;
import github.kasuminova.mmce.client.gui.GuiMEFluidOutputBus;
import github.kasuminova.mmce.client.gui.GuiMEGasInputBus;
import github.kasuminova.mmce.client.gui.GuiMEGasOutputBus;
import github.kasuminova.mmce.client.gui.GuiMEItemInputBus;
import github.kasuminova.mmce.client.gui.GuiMEItemOutputBus;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;

import com.mmcefixes.duck.PollingBus;
import com.mmcefixes.network.FixesNetwork;
import com.mmcefixes.network.PacketOpenPollingGui;


public final class PollingGuiEvents {

    private PollingGuiEvents() {}

    private static boolean isHatchGui(GuiScreen gui) {
        if (gui instanceof GuiMEItemInputBus || gui instanceof GuiMEItemOutputBus
                || gui instanceof GuiMEFluidInputBus || gui instanceof GuiMEFluidOutputBus) return true;

        return Loader.isModLoaded("mekeng") && Loader.isModLoaded("mekanism") && isGasHatchGui(gui);
    }

    @Optional.Method(modid = "mekeng")
    private static boolean isGasHatchGui(GuiScreen gui) {
        return gui instanceof GuiMEGasInputBus || gui instanceof GuiMEGasOutputBus;
    }

    @SubscribeEvent
    public static void addButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isHatchGui(event.getGui())) return;

        GuiContainer gui = (GuiContainer) event.getGui();
        if (!(gui.inventorySlots instanceof AEBaseContainer)) return;

        Object tile = ((AEBaseContainer) gui.inventorySlots).getTileEntity();
        if (!(tile instanceof PollingBus)) return;

        GuiType mainGui = ((PollingBus) tile).mmcefixes$getMainGui();
        int y = gui.getGuiTop() + (mainGui == GuiType.ME_ITEM_OUTPUT_BUS ? 28 : 8);
        event.getButtonList().add(new GuiMEBusPollingButton(gui.getGuiLeft() - 18, y,
            () -> ((PollingBus) tile).mmcefixes$getPolling().getRate()));
    }

    @SubscribeEvent
    public static void openPolling(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.getButton() instanceof GuiMEBusPollingButton)) return;

        GuiContainer gui = (GuiContainer) event.getGui();
        AEBaseContainer container = (AEBaseContainer) gui.inventorySlots;
        MEMachineComponent host = (MEMachineComponent) container.getTileEntity();
        FixesNetwork.CHANNEL.sendToServer(new PacketOpenPollingGui(host.getPos(), false, container.windowId));
        event.setCanceled(true);
    }
}
