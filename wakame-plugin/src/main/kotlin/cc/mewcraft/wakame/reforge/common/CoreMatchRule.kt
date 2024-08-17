package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.io.IOException
import java.lang.reflect.Type
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import java.util.stream.Stream

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


/* Implementations */


/**
 * 用于测试空核心. 可能用不到?
 */
private data object CoreMatchRuleEmpty : CoreMatchRule {
    override val path: Pattern = "empty".toPattern()
    override val priority: Int = Int.MIN_VALUE + 1
    override fun test(core: Core): Boolean {
        return core is CoreEmpty
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("priority", priority),
    )

    override fun toString(): String = toSimpleString()
}


/**
 * 可以匹配所有核心的匹配规则.
 */
private data object CoreMatchRuleAny : CoreMatchRule {
    override val path: Pattern = "[a-z0-9/._-]+".toPattern()
    override val priority: Int = Int.MIN_VALUE
    override fun test(core: Core): Boolean = true // 永远返回 true

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("priority", priority),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 用于测试属性核心.
 */
private class CoreMatchRuleAttribute(
    override val path: Pattern,
    val operation: AttributeModifier.Operation?,
    val element: Key?,
) : CoreMatchRule {
    override val priority: Int = 1

    override fun test(core: Core): Boolean {
        // 开发日记 2024/7/19
        // 正则表达式可以写任意的东西, 使得这里的 Core
        // 可能不是一个 CoreAttribute, 但是这里的代码
        // 仍然假设 core 是一个 CoreAttribute, 这可能
        // 会导致运行时错误.
        //
        // 解决方案:
        // 单独处理 Any Core 的情况, 设计一个专门的实现.
        // 这里只负责处理已经是 CoreAttribute 的情况.

        if (core !is CoreAttribute) {
            return false
        }

        val matcher = path.matcher(core.key.value())
        if (!matcher.matches()) {
            return false
        }

        if (operation != null && core.operation != operation) {
            return false
        }

        return element == core.element?.key
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("priority", priority),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 用于测试技能核心.
 */
private class CoreMatchRuleSkill(
    override val path: Pattern,
    val trigger: Trigger,
    val variant: TriggerVariant,
) : CoreMatchRule {
    override val priority: Int = 2

    override fun test(core: Core): Boolean {
        if (core !is CoreSkill) {
            return false
        }

        val matcher = path.matcher(core.key.value())
        if (!matcher.matches()) {
            return false
        }

        return trigger == core.trigger && variant == core.variant
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("priority", priority),
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String = toSimpleString()
}