package com.mmcefixes.polling;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;

import com.mmcefixes.network.FixesNetwork;
import com.mmcefixes.network.PacketOpenPollingGui;
import com.mmcefixes.network.PacketSetPollingRate;


public class GuiMEBusPollingRate extends AEBaseGui {
    private static final int[] STEPS = {1, 20, 1200, 72000, 1728000};
    private static final String[] UNITS = {"t", "s", "m", "h", "d"};

    private final MEMachineComponent host;
    private GuiTabButton originalGui;
    private int currentPollingRate;

    public GuiMEBusPollingRate(InventoryPlayer inventory, MEMachineComponent host) {
        super(new ContainerMEBusPollingRate(inventory, host));
        this.host = host;
    }

    @Override
    public void initGui() {
        super.initGui();
        ((ContainerMEBusPollingRate) inventorySlots).setListener(rate -> currentPollingRate = rate);

        for (int index = 0; index < STEPS.length; index++) {
            int x = guiLeft + 23 + index * 26;
            buttonList.add(new GuiButton(index, x, guiTop + 32, 24, 20, "+1" + UNITS[index]));
            buttonList.add(new GuiButton(index + 5, x, guiTop + 69, 24, 20, "-1" + UNITS[index]));
        }

        originalGui = new GuiTabButton(guiLeft + 154, guiTop, host.getVisualItemStack(),
            host.getVisualItemStack().getDisplayName(), itemRender);
        buttonList.add(originalGui);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("mmcefixes.polling.name"), 8, 6, 0x404040);

        String display = currentPollingRate == 0
            ? I18n.format("mmcefixes.polling.adaptive") : PollingRateUtils.format(currentPollingRate);
        fontRenderer.drawString(display, (xSize - fontRenderer.getStringWidth(display)) / 2, 57, 0xFFFFFF);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        bindTexture("guis/priority.png");
        drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button == originalGui) {
            FixesNetwork.CHANNEL.sendToServer(new PacketOpenPollingGui(host.getPos(), true, inventorySlots.windowId));
            return;
        }

        if (button.id < 0 || button.id >= STEPS.length * 2) return;

        int delta = STEPS[button.id % STEPS.length] * (button.id < STEPS.length ? 1 : -1);
        long result = (long) currentPollingRate + delta;
        currentPollingRate = (int) Math.max(0L, Math.min(Integer.MAX_VALUE, result));
        FixesNetwork.CHANNEL.sendToServer(new PacketSetPollingRate(currentPollingRate, inventorySlots.windowId));
    }
}
