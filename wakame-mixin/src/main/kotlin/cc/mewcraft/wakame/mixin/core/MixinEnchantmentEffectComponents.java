package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.EnchantmentEffectComponentsPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.UnaryOperator;

@Mixin(EnchantmentEffectComponents.class)
public interface MixinEnchantmentEffectComponents {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        EnchantmentEffectComponentsPatch.bootstrap();
    }

    @Invoker("register")
    static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        throw new AssertionError();
    }

}