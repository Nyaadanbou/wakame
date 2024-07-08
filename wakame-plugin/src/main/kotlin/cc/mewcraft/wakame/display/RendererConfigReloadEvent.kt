package cc.mewcraft.wakame.display

/**
 * 发生在 [RendererConfig] 重新载入之后.
 */
class RendererConfigReloadEvent(
    /**
     * 当前所有出现在配置文件中的 [RawTooltipKey].
     */
    val rawTooltipKeys: Set<RawTooltipKey>,
)