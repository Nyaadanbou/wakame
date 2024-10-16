package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.krequire
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * A [Kizami] instance, the full representation of a kizami.
 *
 * The object is essentially a mapping from [amount][Int] to
 * [effect][KizamiEffect] for a [kizami][Kizami].
 */
data class KizamiInstance(
    /**
     * The kizami to which the [effectMap] corresponds.
     */
    val kizami: Kizami,
    /**
     * The effect map of [kizami], which is essentially a mapping
     * from amount to certain effects.
     */
    val effectMap: Map<Int, KizamiEffect>,
) {
    /**
     * Gets the effect by [amount].
     *
     * Returns [EmptyKizamiEffect] if the [amount] has no defined effect.
     */
    fun getEffectBy(amount: Int): KizamiEffect {
        return effectMap[amount] ?: EmptyKizamiEffect
    }
}

/**
 * The serializer of kizami instance.
 *
 * ## Node structure
 *
 * ```yaml
 * <node>:
 *   binary_index: <Byte>
 *   display_name: <MiniMessage String>
 *   styles: <MiniMessage String>
 *   effects:
 *     1: <List of KizamiEffect>
 *     2: <List of KizamiEffect>
 *     ...
 *     N: <List of KizamiEffect>
 * ```
 */
object KizamiInstanceSerializer : SchemaSerializer<KizamiInstance> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiInstance {
        val kizami = node.krequire<Kizami>()
        val effectMap = buildMap {

            // Add entries: <amount> to <effect list>
            node.node("effects")
                .childrenMap() // Int -> KizamiEffect
                .mapKeys { it.key.toString().toIntOrNull()?.takeIf { amount -> amount > 0 } ?: throw SerializationException(node, type, "The node key must be a positive integer") }
                .forEach { (amount, childNode) ->
                    childNode.hint(KizamiSerializer.HINT_KEY, kizami.key)
                    val kizamiEffect = childNode.krequire<KizamiEffect>()
                    this[amount] = kizamiEffect
                }
        }.let(::Int2ObjectOpenHashMap)

        return KizamiInstance(kizami, effectMap)
    }
}
