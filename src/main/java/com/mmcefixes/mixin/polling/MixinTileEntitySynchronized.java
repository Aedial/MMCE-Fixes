package com.mmcefixes.mixin.polling;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;

import com.mmcefixes.duck.PollingBus;


@Mixin(value = TileEntitySynchronized.class, remap = false)
public abstract class MixinTileEntitySynchronized {
    @Unique private final AtomicBoolean mmcefixes$updating = new AtomicBoolean();
    @Unique private final AtomicBoolean mmcefixes$marking = new AtomicBoolean();

    @Inject(method = "markNoUpdate", at = @At("HEAD"))
    private void mmcefixes$resetMarkTask(CallbackInfo ci) {
        mmcefixes$marking.set(false);
    }

    @Inject(method = "notifyUpdate", at = @At("HEAD"))
    private void mmcefixes$resetUpdateTask(CallbackInfo ci) {
        mmcefixes$updating.set(false);
    }

    @Inject(method = "markNoUpdateSync", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$queueMarkTask(CallbackInfo ci) {
        if (!((Object) this instanceof PollingBus)) return;

        if (mmcefixes$marking.compareAndSet(false, true)) {
            ModularMachinery.EXECUTE_MANAGER.addTEMarkNoUpdateTask((TileEntitySynchronized) (Object) this);
        }

        ci.cancel();
    }

    @Inject(method = "markForUpdateSync", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$queueUpdateTask(CallbackInfo ci) {
        if (!((Object) this instanceof PollingBus)) return;

        if (mmcefixes$updating.compareAndSet(false, true)) {
            mmcefixes$marking.set(true);
            ModularMachinery.EXECUTE_MANAGER.addTEUpdateTask((TileEntitySynchronized) (Object) this);
        }

        ci.cancel();
    }

    @Inject(method = "isInUpdateTask", at = @At("HEAD"), cancellable = true)
    private void mmcefixes$readUpdateTask(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PollingBus) cir.setReturnValue(mmcefixes$updating.get());
    }
}
