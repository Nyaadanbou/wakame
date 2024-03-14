package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * A [Kizami] instance, the full representation of a kizami.
 *
 * The object is essentially a mapping from [Int] to [KizamiEffect].
 */
class KizamiInstance(
    /**
     * The kizami to which the [effect] corresponds.
     */
    val kizami: Kizami,
    /**
     * The effect of [kizami].
     */
    val effect: Map<Int, KizamiEffect>,
) {
    /**
     * Gets the effect by [amount].
     *
     * Returns [EmptyKizamiEffect] if the [amount] has no defined effect.
     */
    fun getEffectBy(amount: Int): KizamiEffect {
        return effect[amount] ?: EmptyKizamiEffect
    }

    /**
     * Gets the effect by [amount].
     *
     * Returns `null` if the [amount] has no defined effect.
     */
    fun getEffectByOrNull(amount: Int): KizamiEffect? {
        return effect[amount]
    }
}

object KizamiInstanceSerializer : SchemeSerializer<KizamiInstance> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiInstance {
        val kizami = node.requireKt<Kizami>()
        val effect = buildMap {
            node.node("effects")
                .childrenMap() // Int -> KizamiEffect
                .mapKeys { it.key.toString().toIntOrNull()?.takeIf { amount -> amount > 0 } ?: throw SerializationException(node, type, "The node key must be a positive integer") }
                .forEach { (amount, childNode) ->
                    childNode.hint(KizamiSerializer.UUID_HINT, kizami.uuid) // add kizami UUID hint
                    this[amount] = childNode.requireKt<KizamiEffect>() // add effect mapping
                }
        }
        return KizamiInstance(kizami, effect)
    }
}
