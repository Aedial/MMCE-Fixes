package com.mmcefixes.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import com.mmcefixes.Tags;


/**
 * GUI Factory for the MMCE Fixes mod configuration.
 * <p>
 * Enables in-game modification of configuration values through the
 * Mod Options menu.
 */
public class MMCEFixesGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
        // No initialization needed
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new MMCEFixesConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    /**
     * The configuration GUI screen for the MMCE Fixes mod.
     */
    public static class MMCEFixesConfigGui extends GuiConfig {

        public MMCEFixesConfigGui(GuiScreen parentScreen) {
            super(
                parentScreen,
                getConfigElements(),
                Tags.MODID,
                false,
                false,
                I18n.format(Tags.MODID + ".config.title")
            );
        }

        /**
         * Gets all configuration elements for display in the GUI.
         *
         * @return List of config elements
         */
        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> elements = new ArrayList<>();

            elements.add(ConfigElement.from(MMCEFixesCommonConfig.class));
            elements.add(ConfigElement.from(MMCEFixesClientConfig.class));

            return elements;
        }
    }
}
