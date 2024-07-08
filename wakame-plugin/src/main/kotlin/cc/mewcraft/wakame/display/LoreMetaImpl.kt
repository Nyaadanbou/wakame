package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

/**
 * 代表一个自定义固定内容的 [LoreMeta].
 */
internal data class CustomConstantLoreMeta(
    override val rawTooltipIndex: RawTooltipIndex,
    override val companionNamespace: String?,
    override val components: List<Component>,
) : ConstantLoreMeta

/**
 * 代表一个“空白的”固定内容的 [LoreMeta].
 */
internal data class BlankConstantLoreMeta(
    override val rawTooltipIndex: RawTooltipIndex,
    override val companionNamespace: String?,
) : ConstantLoreMeta {
    override val components: List<Component> = listOf(Component.empty())
}
