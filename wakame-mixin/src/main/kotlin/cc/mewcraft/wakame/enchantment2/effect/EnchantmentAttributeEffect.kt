package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.serialization.codec.KoishCodecs
import cc.mewcraft.wakame.util.Identifier
import com.google.common.collect.HashMultimap
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.entity.Player

@JvmRecord
data class EnchantmentAttributeEffect(
    val id: Identifier,
    val attribute: Attribute,
    val amount: LevelBasedValue,
    val operation: AttributeModifier.Operation,
) : EnchantmentSpecialEffect {

    companion object {

        @JvmField
        val CODEC: MapCodec<EnchantmentAttributeEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                KoishCodecs.IDENTIFIER.fieldOf("id").forGetter(EnchantmentAttributeEffect::id),
                KoishCodecs.ATTRIBUTE.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute),
                LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount),
                KoishCodecs.ATTRIBUTE_MODIFIER_OPERATION.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation)
            ).apply(instance, ::EnchantmentAttributeEffect)
        }

    }

    fun apply(player: Player, level: Int, slot: ItemSlot) {
        AttributeMapAccess.INSTANCE.get(player).addTransientModifiers(makeAttributeMap(level, slot))
    }

    fun remove(player: Player, level: Int, slot: ItemSlot) {
        AttributeMapAccess.INSTANCE.get(player).removeModifiers(makeAttributeMap(level, slot))
    }

    private fun getModifierId(suffix: String): Identifier {
        return Identifier.key(this.id.namespace(), this.id.value() + "/" + suffix)
    }

    private fun getModifier(value: Int, suffix: ItemSlot): AttributeModifier {
        return AttributeModifier(
            this.getModifierId(suffix.slotIndex.toString()),
            this.amount.calculate(value).toDouble(),
            this.operation
        )
    }

    private fun makeAttributeMap(level: Int, slot: ItemSlot): HashMultimap<Attribute, AttributeModifier> {
        val hashMultimap = HashMultimap.create<Attribute, AttributeModifier>()
        hashMultimap.put(this.attribute, this.getModifier(level, slot))
        return hashMultimap
    }

}