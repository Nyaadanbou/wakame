package cc.mewcraft.wakame.item.binary.cell.core.empty

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.registry.ITEM_CONFIG_FILE
import net.kyori.adventure.text.Component


internal data object EmptyLoreLine : LoreLine {
    override val key: FullKey = GenericKeys.EMPTY
    override val lines: List<Component> by Configs.YAML[ITEM_CONFIG_FILE].entry<List<Component>>("general", "empty_cell_tooltips")
}

internal data class EmptyLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> = listOf(rawKey)
}
