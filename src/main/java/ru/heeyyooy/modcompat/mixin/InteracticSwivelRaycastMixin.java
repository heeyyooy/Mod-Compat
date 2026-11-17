package ru.heeyyooy.modcompat.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    /**
     * The integrated server can retain an interpolated view vector even after it has
     * accepted the rotation packet. Interactic uses getViewVector(1) for its server
     * raycast, so use the authoritative rotation fields directly on the logical server.
     */
    @Redirect(
            method = "raycastItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",
                    remap = true),
            remap = false)
    private static Vec3 modcompat$useAuthoritativeServerRotation(Entity cameraEntity, float partialTicks) {
        if (cameraEntity.level().isClientSide) {
            return cameraEntity.getViewVector(partialTicks);
        }

        return Vec3.directionFromRotation(cameraEntity.getXRot(), cameraEntity.getYRot());
    }

    /**
     * Keep the block-occlusion check on the exact same direction as the item raycast.
     */
    @Redirect(
            method = "raycastItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;",
                    remap = true),
            remap = false)
    private static HitResult modcompat$pickBlocksAlongServerRotation(
            Entity cameraEntity, double distance, float partialTicks, boolean includeFluids) {
        if (cameraEntity.level().isClientSide) {
            return cameraEntity.pick(distance, partialTicks, includeFluids);
        }

        Vec3 start = cameraEntity.getEyePosition(partialTicks);
        Vec3 direction = Vec3.directionFromRotation(cameraEntity.getXRot(), cameraEntity.getYRot());
        Vec3 end = start.add(direction.scale(distance));
        return cameraEntity.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                cameraEntity));
    }
}
