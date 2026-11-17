package ru.heeyyooy.modcompat.compat;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/** Accesses Spatial GUI's public API without making Spatial GUI a hard dependency. */
public final class SpatialGuiState {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile boolean lookupComplete;
    private static Method isActiveMethod;

    private SpatialGuiState() {
    }

    public static boolean isActive() {
        if (!ModList.get().isLoaded("spatial_gui")) {
            return false;
        }

        resolveApi();
        Method method = isActiveMethod;
        if (method == null) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(method.invoke(null));
        } catch (ReflectiveOperationException | LinkageError exception) {
            isActiveMethod = null;
            LOGGER.warn("[Mod Compat] Could not query the Spatial GUI API; crosshair compatibility is disabled.", exception);
            return false;
        }
    }

    private static void resolveApi() {
        if (lookupComplete) {
            return;
        }

        synchronized (SpatialGuiState.class) {
            if (lookupComplete) {
                return;
            }

            try {
                Class<?> api = Class.forName(
                        "org.tastytrash.spatialGUI.api.SpatialGUIAPI",
                        false,
                        SpatialGuiState.class.getClassLoader());
                isActiveMethod = api.getMethod("isActive");
            } catch (ReflectiveOperationException | LinkageError exception) {
                LOGGER.warn("[Mod Compat] Spatial GUI is present, but its public API could not be found.", exception);
            } finally {
                lookupComplete = true;
            }
        }
    }
}
