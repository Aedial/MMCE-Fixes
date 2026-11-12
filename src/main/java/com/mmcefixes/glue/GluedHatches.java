package com.mmcefixes.glue;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import hellfirepvp.modularmachinery.common.block.BlockInputBus;
import hellfirepvp.modularmachinery.common.block.BlockOutputBus;
import hellfirepvp.modularmachinery.common.tiles.TileItemInputBus;
import hellfirepvp.modularmachinery.common.tiles.TileItemOutputBus;

import com.mmcefixes.Tags;
import com.mmcefixes.config.MMCEFixesCommonConfig;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class GluedHatches {
    public static final PropertyBool GLUED = PropertyBool.create("glued");

    public static Item glue;
    public static Item solvent;

    private GluedHatches() {}

    public static boolean isSupportedBlock(Block block) {
        return block != null && (block.getClass() == BlockInputBus.class || block.getClass() == BlockOutputBus.class);
    }

    public static boolean isSupportedTile(TileEntity tile) {
        return tile != null
            && (tile.getClass() == TileItemInputBus.class || tile.getClass() == TileItemOutputBus.class);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (!MMCEFixesCommonConfig.mixins.gluedHatches) return;

        glue = new ItemBusIOToggle(true, "tube_of_glue");
        solvent = new ItemBusIOToggle(false, "solvent");
        event.getRegistry().registerAll(glue, solvent);
    }
}
