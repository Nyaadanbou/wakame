// 文件说明:
// 在不同重铸系统中通用的 [RendererFormat]

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.TextMetaFactoryPredicate
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.util.styleRecursively
import net.kyori.adventure.extra.kotlin.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 一个用来渲染 [StandaloneCell] 的 [RendererFormat].
 */
@ConfigSerializable
internal data class StandaloneCellRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val historyFormat: List<String>,
    @Setting @Required
    private val overallOrdinal: List<OrdinalIndex>,
) : RendererFormat.Simple {
    override val id: String = "standalone_cell"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    /**
     * @param coreText 核心的文本描述
     * @param modCount 重铸次数
     * @param modLimit 重铸次数上限
     */
    fun render(
        coreText: List<Component>,
        modCount: Int,
        modLimit: Int,
    ): IndexedText {
        // 生成重铸历史的文本描述
        val historyText = this.historyFormat.map { line ->
            MM.deserialize(
                line,
                Formatter.number("mod_count", modCount),
                Formatter.number("mod_limit", modLimit),
            )
        }

        // 合并成最终的文本描述
        val resultText = buildList {
            overallOrdinal.forEach {
                when (it) {
                    OrdinalIndex.CORE -> addAll(coreText)
                    OrdinalIndex.HISTORY -> addAll(historyText)
                }
            }
        }

        return SimpleIndexedText(index, resultText)
    }

    enum class OrdinalIndex {
        CORE, HISTORY
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

/**
 * 本 class 封装了共同逻辑, 用于渲染将要被重铸的核孔.
 *
 * @param style 应用在核心上的样式
 * @param prefix 应用在核心上的前缀
 * @param suffix 应用在核心上的后缀
 */
@ConfigSerializable
internal data class DifferenceFormat(
    // 我们想让 style 的默认值含义是 [不修改核心原有的样式].
    // 经验证, 不能用 Style.empty(), 因为会清空原有样式.
    // 所以这里用 null 来表示 [不修改核心原有的样式].
    @Setting
    val style: Style? = null,
    @Setting
    val prefix: Component = empty(),
    @Setting
    val suffix: Component = empty(),
) {
    fun process(source: List<Component>): List<Component> {
        return source.map { text -> (prefix + (style?.let { text.styleRecursively(it) } ?: text) + suffix).compact() }
    }
}
