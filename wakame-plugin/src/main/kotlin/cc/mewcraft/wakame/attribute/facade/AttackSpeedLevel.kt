package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableDouble
import com.google.common.collect.ImmutableMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.mp.ThreadLocal
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID

/**
 * 武器攻击速度
 *
 * The states of Attack Speed Level is **finite**. There are only 9 levels:
 * - (0) Extreme slow
 * - (1) Super slow
 * - (2) Very slow
 * - (3) Slow
 * - (4) Normal
 * - (5) Fast
 * - (6) Very fast
 * - (7) Super fast
 * - (8) Extreme fast
 */
class AttackSpeedLevel : KoinComponent, Initializable,
    AttributeFacade<BinaryAttributeValueS<Byte>, SchemeAttributeValueS>,
    AttributeFactory<BinaryAttributeValueS<Byte>> {

    companion object {
        val localBinaryValue: ThreadLocal<BinaryAttributeValueS<Byte>> = ThreadLocal.withInitial {
            BinaryAttributeValueS<Byte>(0, AttributeModifier.Operation.ADDITION)
        }
    }

    override val key: Key = Key.key(Core.ATTRIBUTE_NAMESPACE, "attack_speed_level")

    override fun schemeOf(node: ConfigurationNode): SchemeAttributeValueS {
        // FIXME 确保数值稳定：攻速比较特殊，就那么几个值会存在，但 SchemeValue 存的是 Double
        return SchemeAttributeValueS.deserialize(node)
    }

    override fun generate(scheme: SchemeAttributeValueS, scalingFactor: Int): BinaryAttributeValueS<Byte> {
        // FIXME 确保数值稳定
        val value = scheme.value.calculate(scalingFactor).toStableByte()
        val operation = scheme.operation
        return BinaryAttributeValueS<Byte>(value, operation)
    }

    override fun decode(tag: CompoundShadowTag): BinaryAttributeValueS<Byte> {
        return localBinaryValue.get().apply {
            value = tag.getByte(AttributeTagNames.VALUE)
            operation = AttributeModifier.Operation.byId(tag.getInt(AttributeTagNames.OPERATION))
        }
    }

    override fun encode(binary: BinaryAttributeValueS<Byte>): CompoundShadowTag {
        return compoundShadowTag {
            putByte(AttributeTagNames.VALUE, binary.value.toStableByte())
            putByte(AttributeTagNames.OPERATION, binary.operation.binary)
        }
    }

    override fun createAttributeModifiers(uuid: UUID, value: BinaryAttributeValueS<Byte>): Map<out Attribute, AttributeModifier> {
        val attribute = Attributes.ATTACK_SPEED_LEVEL
        val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
        return ImmutableMap.of(attribute, modifier)
    }

    override fun onPreWorld() {
        AttributeCoreCodecRegistry.register(key, this)
        AttributeFactoryRegistry.register(key, this)
    }
}