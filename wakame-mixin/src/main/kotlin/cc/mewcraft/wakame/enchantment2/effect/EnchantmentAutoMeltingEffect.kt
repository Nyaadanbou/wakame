package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.enchantment2.component.AutoMelting
import cc.mewcraft.wakame.item.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class EnchantmentAutoMeltingEffect(
    val activated: Boolean,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentAutoMeltingEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("activated").forGetter(EnchantmentAutoMeltingEffect::activated)
            ).apply(instance, ::EnchantmentAutoMeltingEffect)
        }
    }

    context(EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += AutoMelting(activated = activated)
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= AutoMelting
        }
    }

}