package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.ecs.configure
import cc.mewcraft.wakame.enchantment2.component.BlastMining
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue

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
        val CODEC: Codec<EnchantmentBlastMiningEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("explosion_power").forGetter(EnchantmentBlastMiningEffect::explosionPower),
                LevelBasedValue.CODEC.fieldOf("min_block_hardness").forGetter(EnchantmentBlastMiningEffect::minBlockHardness)
            ).apply(instance, ::EnchantmentBlastMiningEffect)
        }

    }

    context(_: EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += BlastMining(
                explosionPower.calculate(level),
                minBlockHardness.calculate(level),
            )
        }
    }

    context(_: EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= BlastMining
        }
    }

}