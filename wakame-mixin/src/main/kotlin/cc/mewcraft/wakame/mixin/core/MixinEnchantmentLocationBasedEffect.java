package cc.mewcraft.wakame.mixin.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 该 Mixin 的实际作用尚未确定.
 *
 * @author Ciallo
 */
@Mixin(EnchantmentLocationBasedEffect.class)
public interface MixinEnchantmentLocationBasedEffect {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(Registry<MapCodec<? extends EnchantmentLocationBasedEffect>> registry, CallbackInfoReturnable<MapCodec<? extends EnchantmentLocationBasedEffect>> cir) {
        // 以备不时之需
    }

}
