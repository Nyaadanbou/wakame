package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

internal data class FixedLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : FixedLine

internal data class ItemMetaLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : ItemMetaLine

internal data class AttributeLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : AttributeLine

internal data class AbilityLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : AbilityLine
