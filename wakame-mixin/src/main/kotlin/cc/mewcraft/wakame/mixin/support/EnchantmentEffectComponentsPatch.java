package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.mixin.core.MixinEnchantmentEffectComponents;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

/**
 * 负责注册新的 {@link net.minecraft.world.item.enchantment.EnchantmentEffectComponents}.
 */
public class EnchantmentEffectComponentsPatch {

    DataComponentType<List<EnchantmentAttributeEffect>> KOISH_ATTRIBUTES = MixinEnchantmentEffectComponents.register(
            "koish_attributes", builder -> builder.persistent(EnchantmentAttributeEffect.CODEC.codec().listOf())
    );

    public static void bootstrap() {
        // 用于初始化静态变量
    }

}
