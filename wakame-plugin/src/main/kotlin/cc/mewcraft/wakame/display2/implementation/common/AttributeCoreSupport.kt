// 文件说明:
// 在不同系统中通用的 [AttributeCore] 的实现

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.util.concurrent.ThreadLocalListProvider
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 用于生成 [AttributeCore] 的 [DerivedIndex].
 */
internal fun AttributeCore.computeIndex(namespace: String): DerivedIndex {
    val attribute = attribute
    val indexId = buildString {
        append(attribute.id)
        append('.')
        append(attribute.operation.key)
        attribute.element?.let {
            append('.')
            append(it.uniqueId)
        }
    }
    return Key.key(namespace, indexId)
}

/**
 * 记录了 [AttributeCore] 特定内容的相对位置.
 */
@ConfigSerializable
internal data class AttributeCoreOrdinalFormat(
    @Setting @Required
    val element: List<String>,
    @Setting @Required
    val operation: List<String>,
)

/**
 * 用于渲染 [AttributeCore] 的数值质量的格式.
 */
@ConfigSerializable
internal data class AttributeCoreQualityFormat(
    @Setting(nodeFromParent = true)
    @Required
    val mapping: Map<AttributeCore.Quality, Component>,
) {
    fun get(quality: AttributeCore.Quality): Component {
        return mapping[quality] ?: error("attribute core quality '$quality' is not defined in the renderer config")
    }

    /**
     * 用于给 [AttributeCore] 的文本描述附加上 *数值质量* 的文本.
     *
     * @param input 包含 <desc> 和 <qual> 占位符的字符串
     * @param data 属性核心的数据
     */
    fun decorate(input: String, data: AttributeCore): List<Component> {
        val qual = data.quality
        if (qual != null) {
            val qualText = get(qual.last())
            val fullText = data.description.mapTo(threadLocalList) { ln ->
                MM.deserialize(
                    input,
                    Placeholder.component("desc", ln),
                    Placeholder.component("qual", qualText)
                )
            }
            return fullText
        } else {
            return data.description
        }
    }

    companion object Shared : ThreadLocalListProvider<Component>(
        onRead = { clear() },
    ) {
        private val MM = Injector.get<MiniMessage>()
    }
}
