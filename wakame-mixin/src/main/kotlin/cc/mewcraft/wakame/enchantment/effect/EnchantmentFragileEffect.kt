package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.ecs.configure
import cc.mewcraft.wakame.enchantment.component.Fragile
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue

@JvmRecord
data class EnchantmentFragileEffect(
    val multiplier: LevelBasedValue,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentFragileEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("multiplier").forGetter(EnchantmentFragileEffect::multiplier)
            ).apply(instance, ::EnchantmentFragileEffect)
        }

    }

    context(_: EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += Fragile(
                multiplier.calculate(level),
            )
        }
    }

    context(_: EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= Fragile
        }
    }

}