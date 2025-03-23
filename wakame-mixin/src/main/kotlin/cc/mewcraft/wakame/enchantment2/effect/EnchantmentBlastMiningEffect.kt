package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.enchantment2.component.BlastMining
import cc.mewcraft.wakame.item.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

@JvmRecord
data class EnchantmentBlastMiningEffect(
    val explodeLevel: Int,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentBlastMiningEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                ExtraCodecs.intRange(1, 16).fieldOf("explode_level").forGetter(EnchantmentBlastMiningEffect::explodeLevel)
            ).apply(instance, ::EnchantmentBlastMiningEffect)
        }

    }

    context(EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += BlastMining(explodeLevel = explodeLevel)
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= BlastMining
        }
    }

}