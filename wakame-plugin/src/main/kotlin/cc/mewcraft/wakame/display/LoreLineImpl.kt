package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

internal data class FixedLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : FixedLoreLine

internal data class MetaLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : MetaLoreLine

internal data class AttributeLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : AttributeLoreLine

internal data class AbilityLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : AbilityLoreLine
