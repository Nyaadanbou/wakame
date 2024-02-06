package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.getByOrThrow
import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableDouble
import com.google.common.collect.ImmutableMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID

private typealias BinaryValueADR = BinaryAttributeValueVE<Float>
private typealias SchemeValueADR = SchemeAttributeValueVE

/**
 * 元素攻击力倍率 %
 */
@Deprecated(message = "每个属性修饰符均已支持独立的 Operation")
class AttackDamageRate : KoinComponent, Initializable,
    AttributeCoreCodec<BinaryValueADR, SchemeValueADR>,
    AttributeModifierFactory<BinaryValueADR> {

    companion object {
        val localBinaryValue: ThreadLocal<BinaryValueADR> = ThreadLocal.withInitial {
            BinaryValueADR(0F, ElementRegistry.DEFAULT_ELEMENT, AttributeModifier.Operation.ADDITION)
        }
    }

    override val key: Key = Key.key(Core.ATTRIBUTE_NAMESPACE, "attack_damage_rate")

    override fun schemeOf(node: ConfigurationNode): SchemeValueADR {
        return SchemeValueADR.deserialize(node)
    }

    override fun generate(scheme: SchemeValueADR, scalingFactor: Int): BinaryValueADR {
        val value = scheme.value.calculate(scalingFactor).toFloat()
        val element = scheme.element
        val operation = scheme.operation
        return BinaryValueADR(value, element, operation)
    }

    override fun decode(tag: CompoundShadowTag): BinaryValueADR {
        return localBinaryValue.get().apply {
            value = tag.getFloat(AttributeTagNames.VALUE)
            element = ElementRegistry.getByOrThrow(tag.getByte(AttributeTagNames.ELEMENT))
            operation = AttributeModifier.Operation.byId(tag.getInt(AttributeTagNames.OPERATION))
        }
    }

    override fun encode(binary: BinaryValueADR): CompoundShadowTag {
        return compoundShadowTag {
            putFloat(AttributeTagNames.VALUE, binary.value)
            putByte(AttributeTagNames.ELEMENT, binary.element.binary)
            putByte(AttributeTagNames.OPERATION, binary.operation.binary)
        }
    }

    override fun createModifier(uuid: UUID, value: BinaryValueADR): Map<Attribute, AttributeModifier> {
        val element = value.element
        val attribute = Attributes.byElement(element).ATTACK_DAMAGE_RATE
        val attributeModifier = AttributeModifier(id = uuid, amount = value.value.toStableDouble(), operation = value.operation)
        return ImmutableMap.of(attribute, attributeModifier)
    }

    override fun onPreWorld() {
        AttributeCoreCodecRegistry.register(key, this)
        AttributeModifierFactoryRegistry.register(key, this)
    }
}