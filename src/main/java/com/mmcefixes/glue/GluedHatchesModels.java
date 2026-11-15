package com.mmcefixes.glue;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import hellfirepvp.modularmachinery.common.lib.BlocksMM;

import com.mmcefixes.Tags;
import com.mmcefixes.config.MMCEFixesCommonConfig;


@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public final class GluedHatchesModels {

    private GluedHatchesModels() {}

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (!MMCEFixesCommonConfig.mixins.gluedHatches) return;

        ModelLoader.setCustomStateMapper(BlocksMM.itemInputBus, new GluedBusStateMapper("glued_input_bus"));
        ModelLoader.setCustomStateMapper(BlocksMM.itemOutputBus, new GluedBusStateMapper("glued_output_bus"));
        ModelLoader.setCustomModelResourceLocation(GluedHatches.glue, 0,
            new ModelResourceLocation(GluedHatches.glue.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(GluedHatches.solvent, 0,
            new ModelResourceLocation(GluedHatches.solvent.getRegistryName(), "inventory"));
    }
}
