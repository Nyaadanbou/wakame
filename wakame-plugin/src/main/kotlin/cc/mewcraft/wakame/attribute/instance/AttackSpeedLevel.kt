package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.Tang
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

private typealias BinaryValueASL = AttributeBinaryValueV<Byte>
private typealias SchemeValueASL = AttributeSchemaValueV

/**
 * 武器攻击速度
 *
 * The states of Attack Speed Level is finite. There are only 9 levels:
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
    AttributeTangCodec<BinaryValueASL, SchemeValueASL>,
    AttributeModifierFactory<BinaryValueASL> {

    companion object {
        val localBinaryValue: ThreadLocal<BinaryValueASL> = ThreadLocal.withInitial {
            BinaryValueASL(0, AttributeModifier.Operation.ADDITION)
        }
    }

    override val key: Key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "attack_speed_level")

    override fun schemeOf(node: ConfigurationNode): SchemeValueASL {
        // FIXME 确保数值稳定：攻速比较特殊，就那么几个值会存在，但 SchemeValue 存的是 Double
        return SchemeValueASL.deserialize(node)
    }

    override fun generate(scheme: SchemeValueASL, scalingFactor: Int): BinaryValueASL {
        // FIXME 确保数值稳定
        val value = scheme.value.calculate(scalingFactor).toStableByte()
        val operation = scheme.operation
        return BinaryValueASL(value, operation)
    }

    override fun decode(tag: CompoundShadowTag): BinaryValueASL {
        return localBinaryValue.get().apply {
            value = tag.getByte(AttributeTags.VALUE)
            operation = AttributeModifier.Operation.byId(tag.getInt(AttributeTags.OPERATION))
        }
    }

    override fun encode(binary: BinaryValueASL): CompoundShadowTag {
        return compoundShadowTag {
            putByte(AttributeTags.VALUE, binary.value)
            putByte(AttributeTags.OPERATION, binary.operation.binary)
        }
    }

    override fun createModifier(uuid: UUID, value: BinaryValueASL): Map<out Attribute, AttributeModifier> {
        val attribute = Attributes.ATTACK_SPEED_LEVEL
        val modifier = AttributeModifier(id = uuid, amount = value.value.toStableDouble(), operation = value.operation)
        return ImmutableMap.of(attribute, modifier)
    }

    override fun onPreWorld() {
        AttributeTangCodecRegistry.register(key, this)
        AttributeModifierFactoryRegistry.register(key, this)
    }
}