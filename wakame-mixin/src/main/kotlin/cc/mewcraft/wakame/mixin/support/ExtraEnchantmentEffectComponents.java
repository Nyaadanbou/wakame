package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.enchantment.effect.*;
import cc.mewcraft.wakame.mixin.core.InvokerEnchantmentEffectComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * 负责注册新的 {@link net.minecraft.world.item.enchantment.EnchantmentEffectComponents}.
 * <p>
 * 程序上, 这些类型是纯粹的数据类型. 这些类型也许会包含一些逻辑, 但这些逻辑最终是要被魔咒的框架调用.
 * 这些数据类型可以通过原版的数据包来创建, 然后便可以通过 {@link Enchantment#effects()} 来访问.
 * <p>
 * 这些新注册的数据类型在数据包中的大概样子如下:
 * <pre>
 * {@code
 * {
 *   // 这是魔咒定义中的 `effects` 字段, 参考: https://minecraft.wiki/w/Enchantment_definition#JSON_format
 *   "effects": {
 *     "koish:attributes": (取决于 Codec) // id 为 "attributes" 的魔咒配置项
 *     "koish:auto_melting": (取决于 Codec) // id 为 "auto_melting" 的魔咒配置项
 *   }
 * }
 * }
 * </pre>
 */
public class ExtraEnchantmentEffectComponents {

    /**
     * 提供自定义属性.
     */
    public static final DataComponentType<List<EnchantmentAttributeEffect>> ATTRIBUTES = register(
            "koish:attributes", builder -> builder.persistent(EnchantmentAttributeEffect.CODEC.codec().listOf())
    );

    /**
     * 如果目标方块可烧炼, 挖掘后掉落熔炼后的物品.
     */
    public static final DataComponentType<EnchantmentSmelterEffect> SMELTER = register(
            "koish:smelter", builder -> builder.persistent(EnchantmentSmelterEffect.CODEC)
    );

    /**
     * 每次挖掘有概率产生爆炸, 挖掘范围内的所有方块.
     */
    public static final DataComponentType<EnchantmentBlastMiningEffect> BLAST_MINING = register(
            "koish:blast_mining", builder -> builder.persistent(EnchantmentBlastMiningEffect.CODEC)
    );

    /**
     * 使物品始终损耗指定倍数的耐久度.
     */
    public static final DataComponentType<EnchantmentFragileEffect> FRAGILE = register(
            "koish:fragile", builder -> builder.persistent(EnchantmentFragileEffect.CODEC)
    );

    /**
     * 挖掘矿物时, 自动挖掘与其相邻的同种矿物.
     */
    public static final DataComponentType<EnchantmentVeinminerEffect> VEINMINER = register(
            "koish:veinminer", builder -> builder.persistent(EnchantmentVeinminerEffect.CODEC)
    );

    /**
     * 玩家射出的弹射物不受重力影响.
     */
    public static final DataComponentType<EnchantmentAntigravShotEffect> ANTIGRAV_SHOT = register(
            "koish:antigrav_shot", builder -> builder.persistent(EnchantmentAntigravShotEffect.CODEC)
    );

    /**
     * 使玩家在受到虚空伤害时传送到附近安全的位置.
     */
    public static final DataComponentType<EnchantmentVoidEscapeEffect> VOID_ESCAPE = register(
            "koish:void_escape", builder -> builder.persistent(EnchantmentVoidEscapeEffect.CODEC)
    );

    public static void bootstrap() {
        // 用于初始化静态变量
    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return InvokerEnchantmentEffectComponents.register(id, builderOperator);
    }

}
