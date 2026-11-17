package ru.heeyyooy.modcompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.heeyyooy.modcompat.compat.SpatialGuiState;

/** Prevents Swivel from re-enabling the world crosshair while Spatial GUI is drawing a 3D screen. */
@Pseudo
@Mixin(targets = "dev.swivel.client.SwivelRuntime", remap = false)
public abstract class SwivelSpatialGuiCrosshairMixin {
    @Shadow
    public static boolean active() {
        throw new AssertionError("Shadow method should be replaced by Mixin");
    }

    @Inject(method = "crosshair", at = @At("RETURN"), cancellable = true, remap = false)
    private static void swivelInteracticCompat$hideCrosshairInSpatialGui(
            boolean vanilla,
            CallbackInfoReturnable<Boolean> cir) {
        if (active() && SpatialGuiState.isActive()) {
            cir.setReturnValue(false);
        }
    }
}
