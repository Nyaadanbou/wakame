package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.ExtraDataComponents;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataComponents.class)
public abstract class MixinDataComponents {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        ExtraDataComponents.bootstrap();
    }
}
