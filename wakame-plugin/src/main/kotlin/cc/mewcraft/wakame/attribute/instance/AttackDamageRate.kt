package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.Tang
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

private typealias BinaryValueADR = AttributeBinaryValueVE<Float>
private typealias SchemeValueADR = AttributeSchemaValueVE

/**
 * 元素攻击力倍率 %
 */
@Deprecated(message = "每个属性修饰符均已支持独立的 Operation")
class AttackDamageRate : KoinComponent, Initializable,
    AttributeTangCodec<BinaryValueADR, SchemeValueADR>,
    AttributeModifierFactory<BinaryValueADR> {

    companion object {
        val localBinaryValue: ThreadLocal<BinaryValueADR> = ThreadLocal.withInitial {
            BinaryValueADR(0F, ElementRegistry.DEFAULT_ELEMENT, AttributeModifier.Operation.ADDITION)
        }
    }

    override val key: Key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "attack_damage_rate")

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
            value = tag.getFloat(AttributeTags.VALUE)
            element = ElementRegistry.getByOrThrow(tag.getByte(AttributeTags.ELEMENT))
            operation = AttributeModifier.Operation.byId(tag.getInt(AttributeTags.OPERATION))
        }
    }

    override fun encode(binary: BinaryValueADR): CompoundShadowTag {
        return compoundShadowTag {
            putFloat(AttributeTags.VALUE, binary.value)
            putByte(AttributeTags.ELEMENT, binary.element.binary)
            putByte(AttributeTags.OPERATION, binary.operation.binary)
        }
    }

    override fun createModifier(uuid: UUID, value: BinaryValueADR): Map<Attribute, AttributeModifier> {
        val element = value.element
        val attribute = Attributes.byElement(element).ATTACK_DAMAGE_RATE
        val attributeModifier = AttributeModifier(id = uuid, amount = value.value.toStableDouble(), operation = value.operation)
        return ImmutableMap.of(attribute, attributeModifier)
    }

    override fun onPreWorld() {
        AttributeTangCodecRegistry.register(key, this)
        AttributeModifierFactoryRegistry.register(key, this)
    }
}