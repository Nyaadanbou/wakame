package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * The empty kizami effect.
 */
data object EmptyKizamiEffect : KizamiEffect {
    override val effects: List<KizamiEffect.Single<*>> = emptyList()
    override fun apply(kizami: Kizami, user: User<*>) {}
    override fun remove(kizami: Kizami, user: User<*>) {}
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
 *
 * ## Node structure
 *
 * ```yaml
 * <node>:
 *   - <effect key>
 *     <impl_defined>
 *   ...
 *   - <effect key>
 *     <impl_defined>
 * ```
 */
object KizamiEffectSerializer : SchemaSerializer<KizamiEffect> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiEffect {
        if (!node.isList) {
            throw SerializationException(node, type, "Node must be a list")
        }

        // get the kizami UUID we are dealing with (UUID is necessary to create attribute modifiers)
        val uuid = node.ownHint(KizamiSerializer.UUID_HINT) ?: throw SerializationException(node, type, "No provided hint for UUID")

        // the collection of effects we are going to deserialize
        val collection = mutableListOf<KizamiEffect.Single<*>>()

        // add each single effect to the collection
        node.childrenList().forEach { childNode ->
            val key = childNode.node("key").krequire<Key>()
            val namespace = key.namespace()
            when (namespace) {
                Namespaces.SKILL -> {
                    val skillCore = BinarySkillCore(childNode)
                    val skillInstance = SkillRegistry.INSTANCE[skillCore.instance]
                    collection += KizamiSkill(skillInstance)
                }

                Namespaces.ATTRIBUTE -> {
                    val attributeCore = BinaryAttributeCore(childNode)
                    val attributeModifiers = attributeCore.provideAttributeModifiers(uuid)
                    collection += KizamiAttribute(attributeModifiers)
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
 * A [skill][ConfiguredSkill] provided by a kizami.
 */
data class KizamiSkill(
    override val effect: ConfiguredSkill,
) : KizamiEffect.Single<ConfiguredSkill> {
    override fun apply(kizami: Kizami, user: User<*>) {
        println("applied kizami (skill) to ${user.uniqueId}") // TODO actually implement it when skill module is done
    }

    override fun remove(kizami: Kizami, user: User<*>) {
        println("removed kizami (skill) from ${user.uniqueId}")
    }
}

/**
 * An [attribute modifier][AttributeModifier] provided by a kizami.
 */
data class KizamiAttribute(
    override val effect: Map<Attribute, AttributeModifier>,
) : KizamiEffect.Single<Map<Attribute, AttributeModifier>> {
    override fun apply(kizami: Kizami, user: User<*>) {
        effect.forEach { (attribute, modifier) -> user.attributeMap[attribute]?.addModifier(modifier) }
    }

    override fun remove(kizami: Kizami, user: User<*>) {
        effect.forEach { (attribute, modifier) -> user.attributeMap[attribute]?.removeModifier(modifier) }
    }
}
