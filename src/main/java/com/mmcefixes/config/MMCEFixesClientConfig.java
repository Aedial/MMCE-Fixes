package com.mmcefixes.config;

import net.minecraftforge.common.config.Config;

import com.mmcefixes.Tags;


@Config(modid = Tags.MODID, name = Tags.MODID + "-client", category = "")
@Config.LangKey(Tags.MODID + ".config.client")
public final class MMCEFixesClientConfig {
    @Config.LangKey("mmcefixes.config.mixins")
    public static MixinsCategory mixins = new MixinsCategory();

    private MMCEFixesClientConfig() {}

    public static class MixinsCategory {
        @Config.Comment("Fix the client-side memory leak when connected to a remote server. Requires restart.")
        @Config.RequiresMcRestart
        @Config.LangKey("mmcefixes.config.clientTasks")
        public boolean clientTasks = true;
    }
}
