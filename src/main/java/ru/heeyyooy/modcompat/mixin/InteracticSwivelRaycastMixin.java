package ru.heeyyooy.modcompat.mixin;

import com.mojang.logging.LogUtils;
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
    private static final org.slf4j.Logger MODCOMPAT_LOGGER = LogUtils.getLogger();
    @Inject(method = "raycastItem", at = @At("HEAD"), remap = false)
    private static void swivelInteracticCompat$beginOrbitAim(
            Entity cameraEntity,
            double reach,
            CallbackInfoReturnable<ItemEntity> cir) {
        if (!cameraEntity.level().isClientSide) {
            MODCOMPAT_LOGGER.info("[Mod Compat] SERVER Interactic raycast started: pos={}, eye={}, yaw={}, pitch={}, look={}, reach={}.",
                    cameraEntity.position(), cameraEntity.getEyePosition(1.0F),
                    cameraEntity.getYRot(), cameraEntity.getXRot(),
                    cameraEntity.getViewVector(1.0F), reach);
        }
        SwivelAimBridge.begin(cameraEntity);
    }

    @Inject(method = "raycastItem", at = @At("RETURN"), remap = false)
    private static void swivelInteracticCompat$restorePlayerRotation(
            Entity cameraEntity,
            double reach,
            CallbackInfoReturnable<ItemEntity> cir) {
        if (!cameraEntity.level().isClientSide) {
            ItemEntity result = cir.getReturnValue();
            MODCOMPAT_LOGGER.info("[Mod Compat] SERVER Interactic raycast finished: itemFound={}, item={}, pos={}, yaw={}, pitch={}, look={}.",
                    result != null,
                    result == null ? "none" : result.getItem().getHoverName().getString(),
                    result == null ? "none" : result.position(),
                    cameraEntity.getYRot(), cameraEntity.getXRot(),
                    cameraEntity.getViewVector(1.0F));
        }
        SwivelAimBridge.end(cameraEntity, cir.getReturnValue());
    }
}
