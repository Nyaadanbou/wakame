package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * The empty kizami effect.
 */
internal data object EmptyKizamiEffect : KizamiEffect {
    override val effects: List<KizamiEffect.Single<*>> = emptyList()
}

/**
 * An immutable kizami effect.
 *
 * @property effects
 */
internal data class SimpleKizamiEffect(
    override val effects: List<KizamiEffect.Single<*>>
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
internal object KizamiEffectSerializer : SchemaSerializer<KizamiEffect> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiEffect {
        if (!node.isList) {
            throw SerializationException(node, type, "Node must be a list")
        }

        // get the kizami id we are dealing with
        val kizamiKey = node.ownHint(KizamiSerializer.HINT_KEY) ?: throw SerializationException(node, type, "No provided hint for key")

        // the collection of effects we are going to deserialize
        val builtEffects = mutableListOf<KizamiEffect.Single<*>>()

        // add each single effect to the collection
        node.childrenList().forEach { childNode ->
            val type1 = childNode.node("type").krequire<Key>()

            when (
                val namespace = type1.namespace()
            ) {
                Namespaces.SKILL -> {
                    val configuredSkillId = type1
                    val configuredSkill = ConfiguredSkill(configuredSkillId, childNode)
                    builtEffects += KizamiSkill(configuredSkill)
                }

                Namespaces.ATTRIBUTE -> {
                    val compositeAttributeId = type1.value()
                    val compositeAttribute = ConstantCompositeAttribute(compositeAttributeId, childNode)
                    val attributeModifiers = compositeAttribute.provideAttributeModifiers(kizamiKey)
                    builtEffects += KizamiAttribute(attributeModifiers)
                }

                else -> {
                    throw SerializationException(node, type, "Unknown kizami effect key '$type1'")
                }
            }
        }

        return SimpleKizamiEffect(builtEffects)
    }
}

/**
 * A [skill][Skill] provided by a kizami.
 */
internal data class KizamiSkill(
    override val effect: ConfiguredSkill,
) : KizamiEffect.Single<ConfiguredSkill> {
    override fun apply(user: User<*>) {
        user.skillMap.addSkill(effect)
    }

    override fun remove(user: User<*>) {
        user.skillMap.removeSkill(effect.id)
    }
}

/**
 * An [attribute modifier][AttributeModifier] provided by a kizami.
 */
internal data class KizamiAttribute(
    override val effect: Map<Attribute, AttributeModifier>,
) : KizamiEffect.Single<Map<Attribute, AttributeModifier>> {
    override fun apply(user: User<*>) {
        effect.forEach { (attribute, modifier) -> user.attributeMap.getInstance(attribute)?.addModifier(modifier) }
    }

    override fun remove(user: User<*>) {
        effect.forEach { (attribute, modifier) -> user.attributeMap.getInstance(attribute)?.removeModifier(modifier) }
    }
}
