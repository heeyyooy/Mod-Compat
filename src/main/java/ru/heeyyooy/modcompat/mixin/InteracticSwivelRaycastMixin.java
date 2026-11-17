package ru.heeyyooy.modcompat.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.heeyyooy.modcompat.compat.SwivelAimBridge;

/** Makes Interactic's custom dropped-item raycast use Swivel's orbit-camera direction. */
@Pseudo
@Mixin(targets = "interactic.util.Helpers", remap = false)
public abstract class InteracticSwivelRaycastMixin {
    @Inject(method = "raycastItem", at = @At("HEAD"), remap = false)
    private static void swivelInteracticCompat$beginOrbitAim(
            Entity cameraEntity,
            double reach,
            CallbackInfoReturnable<ItemEntity> cir) {
        SwivelAimBridge.begin(cameraEntity);
    }

    @Inject(method = "raycastItem", at = @At("RETURN"), remap = false)
    private static void swivelInteracticCompat$restorePlayerRotation(
            Entity cameraEntity,
            double reach,
            CallbackInfoReturnable<ItemEntity> cir) {
        SwivelAimBridge.end(cameraEntity, cir.getReturnValue());
    }
}
