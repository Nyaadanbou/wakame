package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.Fragile
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.entity.LivingEntity

@JvmRecord
data class EnchantmentFragileEffect(
    val multiplier: LevelBasedValue,
) : EnchantmentListenerBasedEffect {
    companion object {

        @JvmField
        val DATA_KEY: MetadataKey<Fragile> = metadataKey<Fragile>("enchantment:fragile")

        @JvmField
        val CODEC: Codec<EnchantmentFragileEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("multiplier").forGetter(EnchantmentFragileEffect::multiplier)
            ).apply(instance, ::EnchantmentFragileEffect)
        }

    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(
            DATA_KEY, Fragile(
                multiplier.calculate(level),
            )
        )
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}