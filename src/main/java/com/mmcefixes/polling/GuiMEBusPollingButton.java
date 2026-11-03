package com.mmcefixes.polling;

import java.util.function.IntSupplier;

import net.minecraft.client.resources.I18n;

import appeng.api.config.FullnessMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;

import com.mmcefixes.config.MMCEFixesConfig;


public class GuiMEBusPollingButton extends GuiImgButton {
    private final IntSupplier rate;

    public GuiMEBusPollingButton(int x, int y, IntSupplier rate) {
        super(x, y, Settings.FULLNESS_MODE, FullnessMode.FULL);
        this.rate = rate;
    }

    @Override
    public String getMessage() {
        int ticks = Math.max(0, rate.getAsInt());
        int min = Math.max(1, MMCEFixesConfig.general.adaptivePollingMin);
        int max = Math.max(min, MMCEFixesConfig.general.adaptivePollingMax);
        String current = ticks == 0
            ? I18n.format("mmcefixes.polling.current_adaptive", min, max)
            : I18n.format("mmcefixes.polling.current", PollingRateUtils.format(ticks));
        String adaptiveFooter = ticks == 0
            ? "\n" + I18n.format("mmcefixes.polling.adaptive_warning")
            : "";

        return I18n.format("mmcefixes.polling.name") + "\n\n" + current + "\n"
            + I18n.format("mmcefixes.polling.tooltip") + adaptiveFooter;
    }
}
