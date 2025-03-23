package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.mixin.support.codec.AttributeCodec
import cc.mewcraft.wakame.mixin.support.codec.AttributeModifierOperationCodec
import cc.mewcraft.wakame.mixin.support.codec.IdentifierCodec
import cc.mewcraft.wakame.util.Identifier
import com.google.common.collect.HashMultimap
import com.mojang.logging.LogUtils
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.entity.Player
import org.slf4j.Logger

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
                IdentifierCodec.INSTANCE.fieldOf("id").forGetter(EnchantmentAttributeEffect::id),
                AttributeCodec.INSTANCE.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute),
                LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount),
                AttributeModifierOperationCodec.INSTANCE.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation)
            ).apply(instance, ::EnchantmentAttributeEffect)
        }

        private val LOGGER: Logger = LogUtils.getLogger()

    }

    fun apply(player: Player, level: Int, slot: ItemSlot) {
        val attributeMapResult = AttributeMapAccess.instance().get(player)
        attributeMapResult.fold(
            { map -> map.addTransientModifiers(makeAttributeMap(level, slot)) },
            { ex -> LOGGER.error("Failed to apply attribute modifier to player: ${ex.message}") }
        )
    }

    fun remove(player: Player, level: Int, slot: ItemSlot) {
        val attributeMapResult = AttributeMapAccess.instance().get(player)
        attributeMapResult.fold(
            { map -> map.removeModifiers(makeAttributeMap(level, slot)) },
            { ex -> LOGGER.error("Failed to remove attribute modifier from player: ${ex.message}") }
        )
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