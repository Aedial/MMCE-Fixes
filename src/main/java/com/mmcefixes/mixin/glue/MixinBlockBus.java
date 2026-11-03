package com.mmcefixes.mixin.glue;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import hellfirepvp.modularmachinery.common.block.BlockBus;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;

import com.mmcefixes.duck.GluedBus;
import com.mmcefixes.glue.GluedHatches;


@Mixin(BlockBus.class)
public abstract class MixinBlockBus extends BlockMachineComponent {

    protected MixinBlockBus(Material material) {
        super(material);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void mmcefixes$defaultGlue(CallbackInfo ci) {
        if (!GluedHatches.isSupportedBlock(this)) return;

        setDefaultState(getDefaultState().withProperty(GluedHatches.GLUED, false));
    }

    @Inject(method = "createBlockState", at = @At("RETURN"), cancellable = true, remap = true)
    private void mmcefixes$addGlueProperty(CallbackInfoReturnable<BlockStateContainer> cir) {
        if (!GluedHatches.isSupportedBlock(this)) return;

        List<IProperty<?>> properties = new ArrayList<>(cir.getReturnValue().getProperties());
        properties.add(GluedHatches.GLUED);
        cir.setReturnValue(new BlockStateContainer(this, properties.toArray(new IProperty<?>[0])));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!GluedHatches.isSupportedBlock(this)) return super.getActualState(state, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        boolean glued = tile instanceof GluedBus && ((GluedBus) tile).mmcefixes$isGlued();
        return state.withProperty(GluedHatches.GLUED, glued);
    }
}
