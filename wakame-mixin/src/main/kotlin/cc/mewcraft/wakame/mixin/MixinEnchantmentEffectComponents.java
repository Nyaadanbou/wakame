package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.ExtraEnchantmentEffectsRegistrar;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentEffectComponents.class)
public interface MixinEnchantmentEffectComponents {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        ExtraEnchantmentEffectsRegistrar.Impl.bootstrap();
    }
}