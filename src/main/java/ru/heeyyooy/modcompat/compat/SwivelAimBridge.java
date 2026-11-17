package ru.heeyyooy.modcompat.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/** Calls Swivel's public aim helpers without requiring Swivel at compile time. */
public final class SwivelAimBridge {
    private static final ThreadLocal<Boolean> AIM_STARTED = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> PICKUP_ACTION = new ThreadLocal<>();

    private static volatile boolean lookupComplete;
    private static Method activeMethod;
    private static Method beginAimMethod;
    private static Method endAimMethod;

    private SwivelAimBridge() {
    }

    public static void beginPickupAction() {
        PICKUP_ACTION.set(Boolean.TRUE);
    }

    public static void endPickupAction() {
        PICKUP_ACTION.remove();
    }

    public static void begin(Entity cameraEntity) {
        AIM_STARTED.remove();

        Minecraft minecraft = Minecraft.getInstance();
        if (cameraEntity == null || cameraEntity != minecraft.player) {
            return;
        }
        if (!ModList.get().isLoaded("swivel")) {
            return;
        }

        resolveApi();
        Method active = activeMethod;
        Method beginAim = beginAimMethod;
        if (active == null || beginAim == null) {
            return;
        }

        try {
            boolean orbitActive = Boolean.TRUE.equals(active.invoke(null));
            if (orbitActive) {
                beginAim.invoke(null, 1.0F);
                AIM_STARTED.set(Boolean.TRUE);
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            AIM_STARTED.remove();
        }
    }

    public static void end(Entity cameraEntity, ItemEntity targetedItem) {
        boolean pickupAction = Boolean.TRUE.equals(PICKUP_ACTION.get());
        PICKUP_ACTION.remove();

        boolean shouldRestore = Boolean.TRUE.equals(AIM_STARTED.get());
        AIM_STARTED.remove();

        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRestore || cameraEntity != minecraft.player) {
            return;
        }

        if (pickupAction && targetedItem != null) {
            // Aim from the player's own eye to the exact item selected by Interactic.
            // This removes the camera/player parallax and keeps the same direction active
            // until Interactic has sent its server-side pickup request.
            Vec3 delta = targetedItem.getBoundingBox().getCenter()
                    .subtract(cameraEntity.getEyePosition(1.0F));
            double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            float yaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));

            cameraEntity.setYRot(yaw);
            cameraEntity.setXRot(pitch);
            cameraEntity.yRotO = yaw;
            cameraEntity.xRotO = pitch;
            if (cameraEntity instanceof LivingEntity livingEntity) {
                livingEntity.setYHeadRot(yaw);
                livingEntity.setYBodyRot(yaw);
            }

            try {
                var connection = minecraft.getConnection();
                if (connection != null) {
                    connection.send(new ServerboundMovePlayerPacket.Rot(
                            yaw, pitch, cameraEntity.onGround()));
                }
            } catch (RuntimeException | LinkageError ignored) {
                // Keep the compatibility layer optional and avoid interrupting pickup handling.
            }

            // Do not call Swivel's endAim here: restoring the old rotation before
            // Interactic sends PickupPayload can make the server raycast miss the item.
            return;
        }

        Method endAim = endAimMethod;
        if (endAim == null) {
            return;
        }

        try {
            endAim.invoke(null);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Swivel is optional; there is no fallback rotation to restore.
        }
    }

    private static void resolveApi() {
        if (lookupComplete) {
            return;
        }

        synchronized (SwivelAimBridge.class) {
            if (lookupComplete) {
                return;
            }

            try {
                Class<?> runtime = Class.forName(
                        "dev.swivel.client.SwivelRuntime",
                        false,
                        SwivelAimBridge.class.getClassLoader());
                activeMethod = runtime.getMethod("active");
                beginAimMethod = runtime.getMethod("beginAim", float.class);
                endAimMethod = runtime.getMethod("endAim");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Swivel is optional and may not expose this API in every version.
            } finally {
                lookupComplete = true;
            }
        }
    }
}
