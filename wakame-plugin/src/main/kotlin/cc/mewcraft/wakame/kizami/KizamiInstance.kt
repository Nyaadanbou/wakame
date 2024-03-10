package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * A [Kizami] instance.
 *
 * The object is essentially a mapping from [Int] to [KizamiEffect].
 */
class KizamiInstance(
    /**
     * The kizami to which the [effect] corresponds.
     */
    private val kizami: Kizami,
    /**
     * The effect of [kizami].
     */
    private val effect: Map<Int, KizamiEffect<*>>,
) {
    /**
     * Gets the effect by [amount].
     *
     * Returns [KizamiEmptyEffect] if the [amount] has no corresponding effect.
     */
    fun getEffectBy(amount: Int): KizamiEffect<*> {
        return effect[amount] ?: KizamiEmptyEffect
    }

    /**
     * Gets the effect by [amount].
     *
     * Returns `null` if the [amount] has no corresponding effect.
     */
    fun getEffectByOrNull(amount: Int): KizamiEffect<*>? {
        return effect[amount]
    }

    companion object Builder {
        fun builder(kizami: Kizami, block: KizamiInstanceBuilder.() -> Unit): KizamiInstance {
            return KizamiInstanceBuilder(kizami).apply(block).build()
        }
    }
}

class KizamiInstanceBuilder(
    private val kizami: Kizami,
) {
    private val effect: MutableMap<Int, KizamiEffect<*>> = HashMap()

    private fun entry(amount: Int, effect: KizamiEffect<*>) {
        this.effect[amount] = effect
    }

    infix fun Int.mapTo(effect: KizamiEffect<*>) {
        entry(this, effect)
    }

    fun build(): KizamiInstance {
        return KizamiInstance(kizami, effect)
    }
}

class KizamiInstanceSerializer : SchemeSerializer<KizamiInstance> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiInstance {
        val kizami = node.requireKt<Kizami>()
        node.node("effects").run {

        }
        TODO()
    }
}
