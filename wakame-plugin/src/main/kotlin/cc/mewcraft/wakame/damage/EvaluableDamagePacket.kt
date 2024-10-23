package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type

data class EvaluableDamageMetadata(
    private val criticalStrikeChance: Evaluable<*>,
    private val criticalStrikePower: Evaluable<*>,
    private val negativeCriticalStrikePower: Evaluable<*>,
    private val damagePackets: List<EvaluableDamagePacket>,
    private val damageTags: DamageTags,
) {
    companion object {
        fun default(): EvaluableDamageMetadata {
            return EvaluableDamageMetadata(
                criticalStrikeChance = Evaluable.parseNumber(0.0),
                criticalStrikePower = Evaluable.parseNumber(1.0),
                negativeCriticalStrikePower = Evaluable.parseNumber(1.0),
                damagePackets = listOf(DefaultEvaluableDamagePacket),
                damageTags = DamageTags.empty()
            )
        }
    }

    fun evaluate(engine: MochaEngine<*>): CustomDamageMetadata {
        return CustomDamageMetadata(
            damageBundle = damageBundle { damagePackets.forEach { single(it.evaluate(engine)) } },
            damageTags = damageTags,
            criticalStrikeMetadata = CriticalStrikeMetadata.byCalculate(
                criticalStrikeChance.evaluate(engine),
                criticalStrikePower.evaluate(engine),
                negativeCriticalStrikePower.evaluate(engine)
            )
        )
    }
}

interface EvaluableDamagePacket {
    fun evaluate(engine: MochaEngine<*>): DamagePacket
}

private data object DefaultEvaluableDamagePacket : EvaluableDamagePacket {
    private val DEFAULT_PACKET by lazy {
        damagePacket(ElementRegistry.DEFAULT) {
            min(1.0)
            max(1.0)
            rate(1.0)
            defensePenetration(.0)
            defensePenetrationRate(.0)
        }
    }

    override fun evaluate(engine: MochaEngine<*>): DamagePacket = DEFAULT_PACKET
}

private data class EvaluableDamagePacketImpl(
    val element: Element,
    val min: Evaluable<*>,
    val max: Evaluable<*>,
    val rate: Evaluable<*>,
    val defensePenetration: Evaluable<*>,
    val defensePenetrationRate: Evaluable<*>,
) : EvaluableDamagePacket {
    override fun evaluate(engine: MochaEngine<*>): DamagePacket {
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
        val damageTagList = node.node("damage_tags").getList<DamageTag>(emptyList())
        val damageTags = DamageTags(damageTagList)

        return EvaluableDamageMetadata(
            criticalStrikeChance = node.node("critical_strike_chance").krequire(),
            criticalStrikePower = node.node("critical_strike_power").krequire(),
            negativeCriticalStrikePower = node.node("negative_critical_strike_power").krequire(),
            damagePackets = node.node("damage_packets").childrenList().map { it.krequire<EvaluableDamagePacket>() },
            damageTags = damageTags
        )
    }
}

internal object EvaluableDamagePacketSerializer : SchemaSerializer<EvaluableDamagePacket> {
    override fun deserialize(type: Type, node: ConfigurationNode): EvaluableDamagePacket {
        val element = node.key()?.toString()?.let { ElementRegistry.INSTANCES.find(it) } ?: throw SerializationException(node, type, "Element ${node.key()} not found")
        return EvaluableDamagePacketImpl(
            element = element,
            min = node.node("min").krequire(),
            max = node.node("max").krequire(),
            rate = node.node("rate").krequire(),
            defensePenetration = node.node("defense_penetration").krequire(),
            defensePenetrationRate = node.node("defense_penetration_rate").krequire()
        )
    }
}