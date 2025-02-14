package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.mixin.support.codec.AttributeCodec
import cc.mewcraft.wakame.mixin.support.codec.AttributeModifierOperationCodec
import cc.mewcraft.wakame.mixin.support.codec.KeyCodec
import com.google.common.collect.HashMultimap
import com.mojang.logging.LogUtils
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.kyori.adventure.key.Key
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.enchantment.EnchantedItemInUse
import net.minecraft.world.item.enchantment.LevelBasedValue
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect
import net.minecraft.world.phys.Vec3
import org.slf4j.Logger

@JvmRecord
data class EnchantmentAttributeEffect(
    val id: Key,
    val attribute: Attribute,
    val amount: LevelBasedValue,
    val operation: AttributeModifier.Operation,
) : EnchantmentLocationBasedEffect {

    companion object {
        @JvmField
        val CODEC: MapCodec<EnchantmentAttributeEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                KeyCodec.INSTANCE.fieldOf("id").forGetter(EnchantmentAttributeEffect::id),
                AttributeCodec.INSTANCE.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute),
                LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount),
                AttributeModifierOperationCodec.INSTANCE.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation)
            ).apply(instance, ::EnchantmentAttributeEffect)
        }

        private val LOGGER: Logger = LogUtils.getLogger()
    }

    override fun onChangedBlock(world: ServerLevel, level: Int, context: EnchantedItemInUse, user: Entity, pos: Vec3, newlyApplied: Boolean) {
        if (newlyApplied && user is ServerPlayer) {
            AttributeMapAccess.instance().get(user.bukkitEntity).fold(
                { map -> map.addTransientModifiers(makeAttributeMap(level, context.inSlot!!)) },
                { ex -> LOGGER.error("Failed to apply attribute modifier to player: ${ex.message}") }
            )
        }
    }

    override fun onDeactivated(context: EnchantedItemInUse, user: Entity, pos: Vec3, level: Int) {
        if (user is ServerPlayer) {
            AttributeMapAccess.instance().get(user.bukkitEntity).fold(
                { map -> map.removeModifiers(makeAttributeMap(level, context.inSlot!!)) },
                { ex -> LOGGER.error("Failed to remove attribute modifier from player: ${ex.message}") }
            )
        }
    }

    override fun codec(): MapCodec<out EnchantmentLocationBasedEffect> {
        return CODEC
    }

    private fun getModifierId(suffix: StringRepresentable): Key {
        return Key.key(this.id.namespace(), this.id.value() + "/" + suffix.serializedName)
    }

    private fun getModifier(value: Int, suffix: StringRepresentable): AttributeModifier {
        return AttributeModifier(
            this.getModifierId(suffix),
            this.amount.calculate(value).toDouble(),
            this.operation
        )
    }

    private fun makeAttributeMap(level: Int, slot: EquipmentSlot): HashMultimap<Attribute, AttributeModifier> {
        val hashMultimap = HashMultimap.create<Attribute, AttributeModifier>()
        hashMultimap.put(this.attribute, this.getModifier(level, slot))
        return hashMultimap
    }

}
