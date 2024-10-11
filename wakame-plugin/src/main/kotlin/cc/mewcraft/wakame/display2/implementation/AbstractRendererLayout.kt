package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.*
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.nio.file.Path

internal abstract class AbstractRendererLayout : RendererLayout, KoinComponent {
    protected val logger: Logger by inject()

    protected val textOrdinalMap: Object2IntOpenHashMap<DerivedIndex> = Object2IntOpenHashMap()
    protected val textMetadataMap: Object2ObjectOpenHashMap<DerivedIndex, TextMeta> = Object2ObjectOpenHashMap()

    init {
        textOrdinalMap.defaultReturnValue(-1)
    }

    fun initialize(layoutPath: Path) {

    }

    /**
     * 获取指定的 [index] 对应的*位置顺序*.
     * 位置顺序由 [DerivedOrdinal] 表示, 数值越小, 越靠前面.
     *
     * 如果 [index] 没有对应的位置顺序, 返回 `null`.
     */
    override fun getOrdinal(index: DerivedIndex): DerivedOrdinal? {
        val ret = textOrdinalMap.getInt(index)
        if (ret == -1) {
            logger.warn("Can't find ordinal for tooltip index '$index'")
            return null
        }
        return ret
    }

    /**
     * 获取指定的 [index] 对应的*元数据*.
     *
     * 如果 [index] 没有对应的元数据, 返回 `null`.
     */
    override fun <T : TextMeta> getMetadata(index: DerivedIndex): T? {
        @Suppress("UNCHECKED_CAST")
        val ret = textMetadataMap[index] as T?
        if (ret == null) {
            logger.warn("Can't find metadata for tooltip index '$index'")
        }
        return ret
    }
}