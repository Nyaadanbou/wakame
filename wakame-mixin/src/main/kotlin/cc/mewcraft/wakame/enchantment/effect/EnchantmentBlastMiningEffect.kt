package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.BlastMining
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.entity.LivingEntity

@JvmRecord
data class EnchantmentBlastMiningEffect(
    /**
     * Explosion power. The more power = the more blocks (area) to explode.
     */
    val explosionPower: LevelBasedValue,
    /**
     * Minimal block hardness for the enchantment to have effect.
     * Block hardness value is how long it takes to break the block by a hand.
     * For example, a Stone has 3.0 hardness.
     */
    val minBlockHardness: LevelBasedValue,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val DATA_KEY: MetadataKey<BlastMining> = metadataKey("enchantment:blast_mining")

        @JvmField
        val CODEC: Codec<EnchantmentBlastMiningEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("explosion_power").forGetter(EnchantmentBlastMiningEffect::explosionPower),
                LevelBasedValue.CODEC.fieldOf("min_block_hardness").forGetter(EnchantmentBlastMiningEffect::minBlockHardness)
            ).apply(instance, ::EnchantmentBlastMiningEffect)
        }
    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(
            DATA_KEY,
            BlastMining(
                explosionPower.calculate(level),
                minBlockHardness.calculate(level),
            )
        )
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}