package cc.mewcraft.wakame.display2

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * 用于查询 [DerivedTooltipIndex] 所对应的:
 * - 文本在提示框里的位置顺序 ([DerivedTooltipOrdinal])
 * - 文本在配置文件里的元数据 ([TextMeta])
 */
class TextMetaQuery(
    private val ordinalMap: Map<DerivedTooltipIndex, DerivedTooltipOrdinal>,
    private val metadataMap: Map<DerivedTooltipIndex, TextMeta>,
) : KoinComponent {
    private val logger: Logger by inject()

    /**
     * 获取指定的 [tooltipIdx] 对应的*位置顺序*.
     * 位置顺序由 [DerivedTooltipOrdinal] 表示, 数值越小, 越靠前面.
     *
     * 如果 [tooltipIdx] 没有对应的位置顺序, 返回 `null`.
     */
    fun getOrdinal(tooltipIdx: DerivedTooltipIndex): DerivedTooltipOrdinal? {
        return ordinalMap[tooltipIdx].also { ordinal ->
            if (ordinal == null) {
                logger.warn("No ordinal found for tooltip index '$tooltipIdx'")
            }
        }
    }

    /**
     * 获取指定的 [tooltipIdx] 对应的*元数据*.
     *
     * 如果 [tooltipIdx] 没有对应的元数据, 返回 `null`.
     */
    fun <T : TextMeta> getMetadata(tooltipIdx: DerivedTooltipIndex): T? {
        @Suppress("UNCHECKED_CAST")
        return (metadataMap[tooltipIdx] as T?).also { metadata ->
            if (metadata == null) {
                logger.warn("No metadata found for tooltip index '$tooltipIdx'")
            }
        }
    }
}
