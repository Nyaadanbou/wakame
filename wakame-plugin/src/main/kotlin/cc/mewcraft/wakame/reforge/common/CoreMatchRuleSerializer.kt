package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.io.IOException
import java.lang.reflect.Type
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * [CoreMatchRule] 的序列化器.
 *
 * 依赖的序列化器:
 * - [cc.mewcraft.wakame.skill.trigger.SkillTriggerSerializer]
 * - [cc.mewcraft.wakame.skill.ConfiguredSkillVariantSerializer]
 */
internal object CoreMatchRuleSerializer : TypeSerializer<CoreMatchRule> {
    override fun deserialize(type: Type, node: ConfigurationNode): CoreMatchRule {
        val typeNode = node.node("type")
        val rawType = typeNode.string ?: throw SerializationException(typeNode, javaTypeOf<String>(), "Missing key: 'type'")
        if (rawType == "*") {
            // 值为 “*” - 直接解析为 CoreMatchRuleAny
            return CoreMatchRuleAny
        }

        val (namespace, pattern) = run {
            val index = rawType.indexOf(':')
            if (index == -1) {
                throw SerializationException(typeNode, javaTypeOf<String>(), "Invalid type: '$rawType'")
            }
            val namespace = rawType.substring(0, index)
            val patternString = rawType.substring(index + 1)
            val pattern = try {
                patternString.toPattern()
            } catch (e: PatternSyntaxException) {
                throw SerializationException(typeNode, javaTypeOf<Pattern>(), e)
            } catch (e: Throwable) {
                throw IOException("Unknown error", e)
            }
            namespace to pattern
        }

        when (namespace) {
            Namespaces.ATTRIBUTE -> {
                val operation = node.node("operation").get<AttributeModifier.Operation>()
                val element = node.node("element").get<String>()?.let { Key.key("element", it) }
                return CoreMatchRuleAttribute(pattern, operation, element)
            }

            Namespaces.SKILL -> {
                val trigger = node.node("trigger").krequire<Trigger>()
                val variant = node.node("variant").get<TriggerVariant>(TriggerVariant.any())
                return CoreMatchRuleSkill(pattern, trigger, variant)
            }

            else -> {
                throw SerializationException(typeNode, javaTypeOf<String>(), "Unknown namespace: '$namespace'")
            }
        }
    }
}