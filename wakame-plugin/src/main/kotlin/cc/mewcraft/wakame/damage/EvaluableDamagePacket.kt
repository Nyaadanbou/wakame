package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type

data class EvaluableDamageMetadata(
    private val criticalPower: Evaluable<*>,
    private val isCritical: Evaluable<*>,
    private val knockback: Evaluable<*>,
    private val damagePackets: List<EvaluableDamagePacket>
) {
    companion object {
        fun default(): EvaluableDamageMetadata {
            return EvaluableDamageMetadata(
                criticalPower = Evaluable.parseNumber(1.0),
                isCritical = Evaluable.parseNumber(0.0),
                knockback = Evaluable.parseNumber(0.0),
                damagePackets = emptyList()
            )
        }
    }

    fun evaluate(engine: MochaEngine<*>): CustomDamageMetadata {
        return CustomDamageMetadata(
            criticalPower = criticalPower.evaluate(engine),
            isCritical = isCritical.evaluate(engine) > 0.0,
            knockback = knockback.evaluate(engine) > 0.0,
            damageBundle = damageBundle { damagePackets.forEach { single(it.evaluate(engine)) } }
        )
    }
}

data class EvaluableDamagePacket(
    val element: Element,
    val min: Evaluable<*>,
    val max: Evaluable<*>,
    val rate: Evaluable<*>,
    val defensePenetration: Evaluable<*>,
    val defensePenetrationRate: Evaluable<*>,
) {
    fun evaluate(engine: MochaEngine<*>): DamagePacket {
        return damagePacket(element) {
            min(min.evaluate(engine))
            max(max.evaluate(engine))
            rate(rate.evaluate(engine))
            defensePenetration(defensePenetration.evaluate(engine))
            defensePenetrationRate(defensePenetrationRate.evaluate(engine))
        }
    }
}

internal object EvaluableDamageBundleSerializer : SchemaSerializer<EvaluableDamageMetadata> {
    override fun deserialize(type: Type, node: ConfigurationNode): EvaluableDamageMetadata {
        return EvaluableDamageMetadata(
            criticalPower = node.node("critical_power").krequire(),
            isCritical = node.node("is_critical").krequire(),
            knockback = node.node("knockback").krequire(),
            damagePackets = node.node("damage_packets").childrenList().map { it.krequire<EvaluableDamagePacket>() }
        )
    }
}

internal object EvaluableDamagePacketSerializer : SchemaSerializer<EvaluableDamagePacket> {
    override fun deserialize(type: Type, node: ConfigurationNode): EvaluableDamagePacket {
        val element = node.key()?.toString()?.let { ElementRegistry.INSTANCES.find(it) } ?: throw SerializationException(node, type, "Element ${node.key()} not found")
        return EvaluableDamagePacket(
            element = element,
            min = node.node("min").krequire(),
            max = node.node("max").krequire(),
            rate = node.node("rate").krequire(),
            defensePenetration = node.node("defense_penetration").krequire(),
            defensePenetrationRate = node.node("defense_penetration_rate").krequire()
        )
    }
}