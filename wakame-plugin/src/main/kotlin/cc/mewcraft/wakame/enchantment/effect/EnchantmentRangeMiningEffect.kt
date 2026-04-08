package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.RangeMining
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.entity.LivingEntity

/**
 * 范围挖掘魔咒效果组件.
 *
 * 数据包配置示例:
 * ```json
 * {
 *   "effects": {
 *     "koish:range_mining": {
 *       "width": 3,
 *       "height": 3,
 *       "depth": 1,
 *       "min_block_hardness": 1.0,
 *       "require_same_type": false
 *     }
 *   }
 * }
 * ```
 *
 * @param width 挖掘范围宽度 (a), 根据 level 计算
 * @param height 挖掘范围高度 (b), 根据 level 计算
 * @param depth 挖掘范围深度 (c), 根据 level 计算
 * @param minBlockHardness 最低方块硬度, 根据 level 计算
 * @param requireSameType 是否只挖掘与中心方块类型相同的方块
 * @param period 每次破坏方块之间的间隔 (tick)
 */
@JvmRecord
data class EnchantmentRangeMiningEffect(
    val width: LevelBasedValue,
    val height: LevelBasedValue,
    val depth: LevelBasedValue,
    val minBlockHardness: LevelBasedValue,
    val requireSameType: Boolean,
    val period: Long,
) : EnchantmentListenerBasedEffect {
    companion object {

        @JvmField
        val DATA_KEY: MetadataKey<RangeMining> = metadataKey("enchantment:range_mining")

        @JvmField
        val CODEC: Codec<EnchantmentRangeMiningEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("width").forGetter(EnchantmentRangeMiningEffect::width),
                LevelBasedValue.CODEC.fieldOf("height").forGetter(EnchantmentRangeMiningEffect::height),
                LevelBasedValue.CODEC.fieldOf("depth").forGetter(EnchantmentRangeMiningEffect::depth),
                LevelBasedValue.CODEC.fieldOf("min_block_hardness").forGetter(EnchantmentRangeMiningEffect::minBlockHardness),
                Codec.BOOL.optionalFieldOf("require_same_type", false).forGetter(EnchantmentRangeMiningEffect::requireSameType),
                Codec.LONG.optionalFieldOf("period", 1L).forGetter(EnchantmentRangeMiningEffect::period),
            ).apply(instance, ::EnchantmentRangeMiningEffect)
        }
    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(
            DATA_KEY,
            RangeMining(
                width.calculate(level).toInt().coerceAtLeast(1),
                height.calculate(level).toInt().coerceAtLeast(1),
                depth.calculate(level).toInt().coerceAtLeast(1),
                minBlockHardness.calculate(level),
                requireSameType,
                period.coerceAtLeast(1),
            )
        )
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}