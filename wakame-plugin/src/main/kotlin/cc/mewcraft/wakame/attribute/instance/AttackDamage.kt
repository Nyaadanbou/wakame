package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.getByOrThrow
import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableDouble
import cc.mewcraft.wakame.util.toStableShort
import com.google.common.collect.ImmutableMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID

private typealias BAV_AD = BinaryAttributeValueLUE<Short>
private typealias SAV_AD = SchemeAttributeValueLUE

/**
 * 元素攻击力
 */
class AttackDamage : KoinComponent, Initializable,
    AttributeCoreCodec<BAV_AD, SAV_AD>,
    AttributeModifierFactory<BAV_AD> {

    companion object {
        val localBinaryValue: ThreadLocal<BAV_AD> = ThreadLocal.withInitial {
            BAV_AD(0, 0, ElementRegistry.DEFAULT_ELEMENT, Operation.ADDITION)
        }
    }

    override val key: Key = Key.key(Core.ATTRIBUTE_NAMESPACE, "attack_damage")

    override fun schemeOf(node: ConfigurationNode): SAV_AD {
        return SAV_AD.deserialize(node)
    }

    override fun generate(scheme: SAV_AD, scalingFactor: Int): BAV_AD {
        val lower = scheme.lower.calculate(scalingFactor).toStableShort()
        val upper = scheme.upper.calculate(scalingFactor).toStableShort()
        val element = scheme.element
        val operation = scheme.operation
        return BAV_AD(lower, upper, element, operation)
    }

    override fun decode(tag: CompoundShadowTag): BAV_AD {
        // FIXME 该函数无法很好的做抽象，因为每个属性存到 NBT 里的数据类型可能不一样
        //  如果真要做抽象，比如共享代码，那也只能把每种数据类型的实现都枚举一遍
        //  不好用反射，因为该函数会极高频率调用，而反射性能没有静态编译好
        return localBinaryValue.get().apply {
            lower = tag.getShort(AttributeTagNames.MIN_VALUE)
            upper = tag.getShort(AttributeTagNames.MAX_VALUE)
            element = ElementRegistry.getByOrThrow(tag.getByte(AttributeTagNames.ELEMENT))
            operation = Operation.byId(tag.getInt(AttributeTagNames.OPERATION))
        }
    }

    override fun encode(binary: BAV_AD): CompoundShadowTag {
        return compoundShadowTag {
            putShort(AttributeTagNames.MIN_VALUE, binary.lower)
            putShort(AttributeTagNames.MAX_VALUE, binary.upper)
            putByte(AttributeTagNames.ELEMENT, binary.element.binary)
            putByte(AttributeTagNames.OPERATION, binary.operation.binary)
        }
    }

    override fun createModifier(uuid: UUID, value: BAV_AD): Map<out Attribute, AttributeModifier> {
        val elem = value.element
        val minAtk = Attributes.byElement(elem).MIN_ATTACK_DAMAGE
        val maxAtk = Attributes.byElement(elem).MAX_ATTACK_DAMAGE
        val operation = value.operation
        val minAtkMod = AttributeModifier(id = uuid, amount = value.lower.toStableDouble(), operation = operation)
        val maxAtkMod = AttributeModifier(id = uuid, amount = value.upper.toStableDouble(), operation = operation)
        return ImmutableMap.of(minAtk, minAtkMod, maxAtk, maxAtkMod)
    }

    // Nova 实现的 Initializer 中有 dependsOn 和 runBefore 等管理依赖加载顺序的机制
    // 但这些机制其实是在造轮子 - 因为 DI 框架会为我自动的管理好类的加载顺序
    // 我只需要无脑的 by inject() 就行了
    override fun onPreWorld() {
        AttributeCoreCodecRegistry.register(key, this)
        AttributeModifierFactoryRegistry.register(key, this)
    }
}
