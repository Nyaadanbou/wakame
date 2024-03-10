package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.Multimap
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * Represents a collection of effects provided by a kizami.
 *
 * See the subtypes for implementation details.
 */
sealed interface KizamiEffect<T> {
    // /**
    //  * The kizami.
    //  */
    // val kizami: Kizami

    /**
     * The effects, such as attributes (modifiers) and skills.
     */
    val effects: T

    /**
     * Applies the [kizami effects][effects] to the [map].
     */
    fun apply(kizami: Kizami, map: KizamiMap)
}

/**
 * The serializer of kizami effect.
 */
object KizamiEffectSerializer : SchemeSerializer<KizamiEffect<*>> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiEffect<*> {
        if (!node.isList) {
            throw SerializationException(node, type, "Node is not a list")
        }

        node.childrenList().forEach { effectNode ->
            val key = node.node("key").requireKt<Key>()
            when {
                key.namespace() == NekoNamespaces.ABILITY -> {

                }

                key.namespace() == NekoNamespaces.ATTRIBUTE -> {

                }

                else -> {
                    throw SerializationException(node, type, "Unknown kizami effect key '$key'")
                }
            }
        }

        TODO()
    }
}

/**
 * The effects of nothing.
 */
data object KizamiEmptyEffect : KizamiEffect<Nothing> {
    // override val kizami: Kizami get() = error("KizamiEmptyEffect has no specified effects")
    override val effects: Nothing get() = error("KizamiEmptyEffect has no specified effects")
    override fun apply(kizami: Kizami, map: KizamiMap) = Unit
}

/**
 * The [skills][Ability] provided by kizami.
 */
data class KizamiSkill(
    // override val kizami: Kizami,
    override val effects: Set<Ability>,
) : KizamiEffect<Set<Ability>> {
    override fun apply(kizami: Kizami, map: KizamiMap) {
        TODO("Not yet implemented")
    }
}

/**
 * The [attribute modifiers][AttributeModifier] provided by kizami.
 */
data class KizamiAttribute(
    // override val kizami: Kizami,
    override val effects: Multimap<out Attribute, AttributeModifier>,
) : KizamiEffect<Multimap<out Attribute, AttributeModifier>> {
    override fun apply(kizami: Kizami, map: KizamiMap) {
        TODO("Not yet implemented")
    }
}
