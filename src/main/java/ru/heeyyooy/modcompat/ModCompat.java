package ru.heeyyooy.modcompat;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value = ModCompat.MOD_ID, dist = Dist.CLIENT)
public final class ModCompat {
    public static final String MOD_ID = "modcompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModCompat(IEventBus modEventBus) {
        LOGGER.info("[Mod Compat] Loaded. Swivel: {}, Interactic: {}, Spatial GUI: {}",
                ModList.get().isLoaded("swivel"),
                ModList.get().isLoaded("interactic"),
                ModList.get().isLoaded("spatial_gui"));
    }
}
