package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.io.IOException
import java.lang.reflect.Type
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

// 开发日记 2024/7/19
// 如果 namespace 是 *, 那么就是匹配所有的核心.
// 这种情况下, path 是什么已经不重要了,
// 因为在 namespace 为任意值的前提之下去指定 path 是没有意义的.
//
// 但是, 如果 namespace 不是 *, 那么 namespace 就是确定的.
// 这种情况下, path 所指的就是这一类核心中的某一类核心.
// 例如, 已经确定 namespace 为 attribute,
// 那么就需要接着指定在其之下的 path, 例如 attack_damage, attack_speed_level 等等.
//
// 结论:
// 1. 如果 namespace 是 *, 那么 path 是不重要的. 这时应该直接返回一个 CoreMatchRuleAny
// 2. 如果 namespace 不是 *, 那么 path 是必须的. 这时应该根据 namespace 来返回对应的 CoreMatchRule

/**
 * 词条栏的核心的匹配规则, 用于测试一个核心是否符合某种规则.
 */
interface CoreMatchRule : Examinable {
    /**
     * 正则表达式, 用于匹配核心的*路径*.
     *
     * 注意, 该正则表达式*不负责*匹配命名空间. 命名空间已经由实现类确定了,
     * 剩下的就是由该正则表达式测试该命名空间下对应的路径.
     */
    val path: Pattern

    /**
     * 该匹配规则的优先级, 数字更小的将被优先执行.
     *
     * 该数值采用硬编码, 不应该由配置文件自定义.
     */
    val priority: Int

    /**
     * 检查 [core] 是否符合该匹配规则.
     */
    fun test(core: Core): Boolean
}

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