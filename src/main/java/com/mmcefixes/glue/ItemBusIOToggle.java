package com.mmcefixes.glue;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import hellfirepvp.modularmachinery.common.CommonProxy;

import com.mmcefixes.Tags;
import com.mmcefixes.duck.GluedBus;


public class ItemBusIOToggle extends Item {
    private final boolean disableIO;

    public ItemBusIOToggle(boolean disableIO, String name) {
        this.disableIO = disableIO;
        setRegistryName(Tags.MODID, name);
        setTranslationKey(Tags.MODID + "." + name);
        setMaxStackSize(1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
            EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof GluedBus) || !GluedHatches.isSupportedTile(tile)) return EnumActionResult.PASS;
        if (!GluedHatches.isSupportedBlock(world.getBlockState(pos).getBlock())) return EnumActionResult.PASS;
        if (!player.canPlayerEdit(pos, facing, player.getHeldItem(hand))) return EnumActionResult.FAIL;

        // Intercept the click before the bus GUI consumes it
        if (world.isRemote) return EnumActionResult.SUCCESS;

        boolean changed = ((GluedBus) tile).mmcefixes$setGlued(disableIO);
        String action = disableIO ? "disabled" : "enabled";
        if (!changed) action = "already_" + action;

        player.sendStatusMessage(new TextComponentTranslation("mmcefixes.glue." + action), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("mmcefixes.glue." + (disableIO ? "disable" : "enable")));
    }
}
