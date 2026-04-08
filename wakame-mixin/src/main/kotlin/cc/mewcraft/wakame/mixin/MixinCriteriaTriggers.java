package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.ExtraCriteriaTriggers;
import net.minecraft.advancements.CriteriaTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CriteriaTriggers.class)
public class MixinCriteriaTriggers {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        ExtraCriteriaTriggers.bootstrap();
    }
}
