package ru.heeyyooy.modcompat.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/** Calls Swivel's public aim helpers without requiring Swivel at compile time. */
public final class SwivelAimBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
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
        LOGGER.info("[Mod Compat] Interactic right-click pickup action entered.");
    }

    public static void endPickupAction() {
        if (Boolean.TRUE.equals(PICKUP_ACTION.get())) {
            LOGGER.warn("[Mod Compat] The right-click action ended without passing through Interactic.Helpers.raycastItem; check Interactic rightClickPickup configuration and Mixin loading.");
        }
        PICKUP_ACTION.remove();
    }

    public static void begin(Entity cameraEntity) {
        AIM_STARTED.remove();

        Minecraft minecraft = Minecraft.getInstance();
        boolean pickupAction = Boolean.TRUE.equals(PICKUP_ACTION.get());
        if (cameraEntity == null || cameraEntity != minecraft.player) {
            if (pickupAction) {
                LOGGER.warn("[Mod Compat] Interactic raycast received a camera entity that is not the local player: {}.",
                        cameraEntity == null ? "null" : cameraEntity.getType());
            }
            return;
        }
        if (!ModList.get().isLoaded("swivel")) {
            if (pickupAction) {
                LOGGER.warn("[Mod Compat] Interactic pickup action detected, but Swivel is not loaded.");
            }
            return;
        }

        resolveApi();
        Method active = activeMethod;
        Method beginAim = beginAimMethod;
        if (active == null || beginAim == null) {
            if (pickupAction) {
                LOGGER.warn("[Mod Compat] Interactic pickup action detected, but Swivel's aim API is unavailable.");
            }
            return;
        }

        try {
            boolean orbitActive = Boolean.TRUE.equals(active.invoke(null));
            if (orbitActive) {
                beginAim.invoke(null, 1.0F);
                AIM_STARTED.set(Boolean.TRUE);
                if (pickupAction) {
                    LOGGER.info("[Mod Compat] Swivel Orbit Camera aim applied for Interactic pickup raycast.");
                }
            } else if (pickupAction) {
                LOGGER.warn("[Mod Compat] Interactic pickup action detected, but Swivel Orbit Camera is inactive.");
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("[Mod Compat] Could not synchronize Interactic's item raycast with Swivel's orbit camera.", exception);
            AIM_STARTED.remove();
        }
    }

    public static void end(Entity cameraEntity, ItemEntity targetedItem) {
        boolean pickupAction = Boolean.TRUE.equals(PICKUP_ACTION.get());
        PICKUP_ACTION.remove();

        boolean shouldRestore = Boolean.TRUE.equals(AIM_STARTED.get());
        AIM_STARTED.remove();

        Minecraft minecraft = Minecraft.getInstance();
        if (pickupAction) {
            LOGGER.info("[Mod Compat] Interactic pickup raycast result: itemFound={}, orbitAimApplied={}.",
                    targetedItem != null, shouldRestore);
        }
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
                    LOGGER.info("[Mod Compat] Sent pickup aim for {} at {} (yaw={}, pitch={}).",
                            targetedItem.getItem().getHoverName().getString(),
                            targetedItem.blockPosition(), yaw, pitch);
                } else {
                    LOGGER.error("[Mod Compat] Could not send pickup aim: client network connection is null.");
                }
            } catch (RuntimeException | LinkageError exception) {
                LOGGER.warn("[Mod Compat] Could not synchronize the item-target rotation before Interactic's pickup request.", exception);
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
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("[Mod Compat] Could not restore the player rotation after Interactic's item raycast.", exception);
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
            } catch (ReflectiveOperationException | LinkageError exception) {
                LOGGER.warn("[Mod Compat] Swivel is present, but its orbit-aim API could not be found.", exception);
            } finally {
                lookupComplete = true;
            }
        }
    }
}
