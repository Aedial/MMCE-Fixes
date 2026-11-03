package com.mmcefixes.mixin.polling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.EntityPlayerMP;

import github.kasuminova.mmce.common.network.PktSwitchGuiMEOutputBus;


@Mixin(value = PktSwitchGuiMEOutputBus.class, remap = false)
public abstract class MixinPktSwitchGuiMEOutputBus {

    @Redirect(method = "lambda$onMessage$0", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/player/EntityPlayerMP;closeScreen()V", remap = true))
    private static void mmcefixes$keepGuiOpen(EntityPlayerMP player) {
    }
}
