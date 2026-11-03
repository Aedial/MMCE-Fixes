package com.mmcefixes.glue;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.item.Item;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.mmcefixes.Tags;
import com.mmcefixes.config.MMCEFixesConfig;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class GluedHatches {
    public static final PropertyBool GLUED = PropertyBool.create("glued");

    public static Item glue;
    public static Item solvent;

    private GluedHatches() {}

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (!MMCEFixesConfig.mixins.gluedHatches) return;

        glue = new ItemBusIOToggle(true, "tube_of_glue");
        solvent = new ItemBusIOToggle(false, "solvent");
        event.getRegistry().registerAll(glue, solvent);
    }
}
