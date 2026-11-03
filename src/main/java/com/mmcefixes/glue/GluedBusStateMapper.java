package com.mmcefixes.glue;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

import com.mmcefixes.Tags;


public class GluedBusStateMapper extends StateMapperBase {
    private final ResourceLocation location;

    public GluedBusStateMapper(String name) {
        location = new ResourceLocation(Tags.MODID, name);
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        return new ModelResourceLocation(location, getPropertyString(state.getProperties()));
    }
}
