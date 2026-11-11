package com.mmcefixes.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.mmcefixes.Tags;


@Mod.EventBusSubscriber(modid = Tags.MODID)
@Config(modid = Tags.MODID, name = Tags.MODID, category = "")
@Config.LangKey(Tags.MODID + ".config.title")
public final class MMCEFixesConfig {
    @Config.LangKey("mmcefixes.config.general")
    public static GeneralCategory general = new GeneralCategory();

    @Config.LangKey("mmcefixes.config.mixins")
    public static MixinsCategory mixins = new MixinsCategory();

    private MMCEFixesConfig() {}

    public static void load() {
        ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (Tags.MODID.equals(event.getModID())) load();
    }

    public static class GeneralCategory {
        @Config.Comment("Glue newly placed item hatches by default. Existing hatches retain their saved setting.")
        @Config.LangKey("mmcefixes.config.gluedByDefault")
        public boolean gluedByDefault = false;

        @Config.Comment("Base capacity of each ME fluid hatch slot, in mB, before Capacity Cards. Requires restart.")
        @Config.RangeInt(min = 1)
        @Config.LangKey("mmcefixes.config.meFluidBusBaseCapacity")
        public int meFluidBusBaseCapacity = 8000;

        @Config.Comment("Base capacity of each ME gas hatch slot before Capacity Cards. Requires restart.")
        @Config.RangeInt(min = 1)
        @Config.LangKey("mmcefixes.config.meGasBusBaseCapacity")
        public int meGasBusBaseCapacity = 8000;

        @Config.Comment("Default polling interval for ME hatches, in ticks. Set 0 for adaptive polling (please don't).")
        @Config.RangeInt(min = 0)
        @Config.LangKey("mmcefixes.config.defaultPollingRate")
        public int defaultPollingRate = 200;

        @Config.Comment("Fastest adaptive polling interval, in ticks.")
        @Config.RangeInt(min = 1)
        @Config.LangKey("mmcefixes.config.adaptivePollingMin")
        public int adaptivePollingMin = 5;

        @Config.Comment("Slowest adaptive polling interval, in ticks. Uses at least the configured minimum.")
        @Config.RangeInt(min = 1)
        @Config.LangKey("mmcefixes.config.adaptivePollingMax")
        public int adaptivePollingMax = 60;

        @Config.Comment("Wait for the slowest adaptive interval before an idle hatch sleeps.")
        @Config.LangKey("mmcefixes.config.adaptivePollingWaitForSleep")
        public boolean adaptivePollingWaitForSleep = true;
    }

    public static class MixinsCategory {
        @Config.Comment("Enable glued item hatches, glue and solvent. Requires restart.")
        @Config.LangKey("mmcefixes.config.gluedHatches")
        public boolean gluedHatches = true;

        @Config.Comment("Enable configurable ME fluid and gas hatch capacity. Requires restart.")
        @Config.LangKey("mmcefixes.config.meFluidSize")
        public boolean meFluidSize = true;

        @Config.Comment("Enable ME hatch polling settings and wake-up fixes. Requires restart.")
        @Config.LangKey("mmcefixes.config.mePolling")
        public boolean mePolling = true;

        @Config.Comment("Drop MMCE main thread tasks on a client connected to a remote server, where they never run and leak worlds. Read by each client. Requires restart.")
        @Config.LangKey("mmcefixes.config.clientTasks")
        public boolean clientTasks = true;
    }
}
