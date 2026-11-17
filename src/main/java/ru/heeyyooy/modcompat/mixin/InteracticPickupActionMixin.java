package ru.heeyyooy.modcompat.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.heeyyooy.modcompat.compat.SwivelAimBridge;

/** Marks the Interactic right-click handler so its item raycast can sync aim before pickup. */
@Mixin(Minecraft.class)
public abstract class InteracticPickupActionMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void modcompat$beginPickupAction(CallbackInfo ci) {
        SwivelAimBridge.beginPickupAction();
    }

    @Inject(method = "startUseItem", at = @At("RETURN"))
    private void modcompat$endPickupAction(CallbackInfo ci) {
        SwivelAimBridge.endPickupAction();
    }
}
