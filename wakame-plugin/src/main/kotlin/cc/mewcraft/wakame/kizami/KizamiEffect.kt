package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.NoopAbility
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * A collection of effects provided by a kizami.
 *
 * See the subtypes for implementation details.
 */
sealed interface KizamiEffect {
    /**
     * The collection of effects, such as attributes (modifiers) and skills.
     */
    val effects: List<Single<*>>

    /**
     * Applies the collection of [kizami effects][Single] to the [map].
     */
    fun apply(kizami: Kizami, map: KizamiMap) {
        effects
    }

    /**
     * A single effect.
     *
     * @param T the effect type
     */
    interface Single<T> {
        /**
         * A single effect.
         */
        val effect: T

        /**
         * Applies the single effect.
         */
        fun apply(kizami: Kizami, map: KizamiMap)
    }
}

/**
 * The empty kizami effect.
 */
data object EmptyKizamiEffect : KizamiEffect {
    override val effects: List<KizamiEffect.Single<*>> = emptyList()
    override fun apply(kizami: Kizami, map: KizamiMap): Unit = Unit
}

/**
 * An immutable kizami effect.
 *
 * @property effects
 */
data class ImmutableKizamiEffect(
    override val effects: List<KizamiEffect.Single<*>>,
) : KizamiEffect

/**
 * The serializer of kizami effect.
 */
object KizamiEffectSerializer : SchemeSerializer<KizamiEffect> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiEffect {
        if (!node.isList) {
            throw SerializationException(node, type, "Node must be a list")
        }

        // get the kizami UUID we are dealing with
        val uuid = node.ownHint(KizamiSerializer.UUID_HINT) ?: throw SerializationException(node, type, "No provided hint for UUID")

        // the collection of effects we are going to deserialize
        val collection = mutableListOf<KizamiEffect.Single<*>>()

        // add each single effect to the collection
        node.childrenList().forEach { childNode ->
            val key = childNode.node("key").requireKt<Key>()
            when {
                key.namespace() == NekoNamespaces.ABILITY -> {
                    collection += KizamiSkill(NoopAbility)
                }

                key.namespace() == NekoNamespaces.ATTRIBUTE -> {
                    val plainData = AttributeRegistry.plainNodeEncoder.getValue(key).encode(childNode)
                    val modifiers = AttributeRegistry.modifierFactory.getValue(key).createAttributeModifiers(uuid, plainData)
                    collection += KizamiAttribute(modifiers)
                }

                else -> {
                    throw SerializationException(node, type, "Unknown kizami effect key '$key'")
                }
            }
        }

        return ImmutableKizamiEffect(collection)
    }
}

/**
 * A [skill][Ability] provided by a kizami.
 */
data class KizamiSkill(
    override val effect: Ability,
) : KizamiEffect.Single<Ability> {
    override fun apply(kizami: Kizami, map: KizamiMap) {
        // TODO("Not yet implemented")
    }
}

/**
 * An [attribute modifier][AttributeModifier] provided by a kizami.
 */
data class KizamiAttribute(
    override val effect: Map<Attribute, AttributeModifier>,
) : KizamiEffect.Single<Map<Attribute, AttributeModifier>> {
    override fun apply(kizami: Kizami, map: KizamiMap) {
        TODO("Not yet implemented")
    }
}
