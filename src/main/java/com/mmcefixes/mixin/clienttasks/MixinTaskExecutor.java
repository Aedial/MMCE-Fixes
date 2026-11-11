package com.mmcefixes.mixin.clienttasks;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import github.kasuminova.mmce.common.concurrent.TaskExecutor;

import com.mmcefixes.Tags;


/**
 * TaskExecutor only drains its main thread queues from onServerTick. A client connected to a remote server never gets
 * a server tick, so every task queued there stays forever, and each one holds its tile entity and that tile's world.
 * This keeps every world the client has left in memory. Without a server in this JVM the tasks could never run anyway,
 * so they are dropped instead of queued.
 */
@Mixin(value = TaskExecutor.class, remap = false)
public abstract class MixinTaskExecutor {
    @Unique private static final Logger mmcefixes$LOGGER = LogManager.getLogger(Tags.MODID);
    @Unique private static boolean mmcefixes$logged;

    @Inject(method = "addSyncTask", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$dropSyncTask(CallbackInfo ci) {
        mmcefixes$dropWithoutServer(ci);
    }

    @Inject(method = "addTEUpdateTask", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$dropTEUpdateTask(CallbackInfo ci) {
        mmcefixes$dropWithoutServer(ci);
    }

    @Inject(method = "addTEMarkNoUpdateTask", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$dropTEMarkNoUpdateTask(CallbackInfo ci) {
        mmcefixes$dropWithoutServer(ci);
    }

    @Unique
    private static void mmcefixes$dropWithoutServer(CallbackInfo ci) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) return;

        if (!mmcefixes$logged) {
            mmcefixes$logged = true;
            mmcefixes$LOGGER.debug("No integrated or dedicated server in this JVM, dropping MMCE main thread tasks");
        }

        ci.cancel();
    }
}
