package ru.heeyyooy.modcompat.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/** Calls Swivel's public aim helpers without requiring Swivel at compile time. */
public final class SwivelAimBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<Boolean> AIM_STARTED = new ThreadLocal<>();

    private static volatile boolean lookupComplete;
    private static Method activeMethod;
    private static Method beginAimMethod;
    private static Method endAimMethod;

    private SwivelAimBridge() {
    }

    public static void begin(Entity cameraEntity) {
        AIM_STARTED.remove();

        Minecraft minecraft = Minecraft.getInstance();
        if (cameraEntity == null || cameraEntity != minecraft.player || !ModList.get().isLoaded("swivel")) {
            return;
        }

        resolveApi();
        Method active = activeMethod;
        Method beginAim = beginAimMethod;
        if (active == null || beginAim == null) {
            return;
        }

        try {
            if (Boolean.TRUE.equals(active.invoke(null))) {
                beginAim.invoke(null, 1.0F);
                AIM_STARTED.set(Boolean.TRUE);
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("[Mod Compat] Could not synchronize Interactic's item raycast with Swivel's orbit camera.", exception);
            AIM_STARTED.remove();
        }
    }

    public static void end(Entity cameraEntity) {
        boolean shouldRestore = Boolean.TRUE.equals(AIM_STARTED.get());
        AIM_STARTED.remove();

        if (!shouldRestore || cameraEntity != Minecraft.getInstance().player) {
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
