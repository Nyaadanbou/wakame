package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 封装了零或多个 [CoreMatchRule] 实例的容器.
 */
fun interface CoreMatchRuleContainer : Examinable {
    companion object {
        /**
         * 获取一个 [CoreMatchRuleContainer] 的特殊实例, 对于所有核心都返回 `true`.
         */
        fun any(): CoreMatchRuleContainer = AnyCoreMatchRuleContainer

        /**
         * 获取一个 [CoreMatchRuleContainer] 的特殊实例, 对于所有核心都返回 `false`.
         */
        fun empty(): CoreMatchRuleContainer = EmptyCoreMatchRuleContainer

        /**
         * 创建一个 [CoreMatchRuleContainer] 的标准实例.
         */
        fun simple(matchers: List<CoreMatchRule>): CoreMatchRuleContainer = SimpleCoreMatchRuleContainer(matchers)
    }

    /**
     * 检查本容器中是否存在一个规则, 能够匹配给定的核心 [core].
     */
    fun test(core: Core): Boolean
}

/**
 * [CoreMatchRuleContainer] 的一般实现.
 */
private class SimpleCoreMatchRuleContainer(
    private val matchers: List<CoreMatchRule>,
) : CoreMatchRuleContainer {
    override fun test(core: Core): Boolean {
        return matchers.any { it.test(core) }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("matchers", matchers)
    )

    override fun toString(): String =
        toSimpleString()
}

private object AnyCoreMatchRuleContainer : CoreMatchRuleContainer {
    override fun test(core: Core): Boolean = true
    override fun toString(): String = "AnyCoreMatchRuleContainer"
}

private object EmptyCoreMatchRuleContainer : CoreMatchRuleContainer {
    override fun test(core: Core): Boolean = false
    override fun toString(): String = "EmptyCoreMatchRuleContainer"
}

/**
 * [CoreMatchRuleContainer] 的序列化器.
 *
 * 依赖的序列化器:
 * - [CoreMatchRuleSerializer]
 */
internal object CoreMatchRuleContainerSerializer : TypeSerializer<CoreMatchRuleContainer> {
    override fun deserialize(type: Type, node: ConfigurationNode): CoreMatchRuleContainer {
        val rules = node.getList<CoreMatchRule>(emptyList())
        return CoreMatchRuleContainer.simple(rules)
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): CoreMatchRuleContainer? {
        return EmptyCoreMatchRuleContainer
    }
}