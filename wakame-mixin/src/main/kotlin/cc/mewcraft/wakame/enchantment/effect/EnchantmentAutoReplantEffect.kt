package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.AutoReplant
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.serialization.codec.BukkitCodecs
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.bukkit.Material
import org.bukkit.entity.LivingEntity

/**
 * 自动种植魔咒效果组件.
 *
 * 数据包配置示例:
 * ```json
 * {
 *   "effects": {
 *     "koish:auto_replant": {
 *       "crop_seed_map": {
 *         "minecraft:wheat": "minecraft:wheat_seeds",
 *         "minecraft:carrots": "minecraft:carrot",
 *         "minecraft:potatoes": "minecraft:potato",
 *         "minecraft:beetroots": "minecraft:beetroot_seeds",
 *         "minecraft:nether_wart": "minecraft:nether_wart"
 *       }
 *     }
 *   }
 * }
 * ```
 *
 * @param cropSeedMap 农作物方块到种子物品的映射
 */
@JvmRecord
data class EnchantmentAutoReplantEffect(
    val cropSeedMap: Map<Material, Material>,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val DATA_KEY: MetadataKey<AutoReplant> = metadataKey("enchantment:auto_replant")

        @JvmField
        val CODEC: Codec<EnchantmentAutoReplantEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.unboundedMap(BukkitCodecs.MATERIAL, BukkitCodecs.MATERIAL)
                    .fieldOf("crop_seed_map")
                    .forGetter(EnchantmentAutoReplantEffect::cropSeedMap),
            ).apply(instance, ::EnchantmentAutoReplantEffect)
        }
    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(DATA_KEY, AutoReplant(cropSeedMap))
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}
