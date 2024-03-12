package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta
import com.google.common.collect.ClassToInstanceMap
import net.kyori.adventure.key.Key
import java.util.UUID

internal data class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val material: Key,
    override val itemMeta: ClassToInstanceMap<SchemeItemMeta<*>>,
    override val cells: Map<String, SchemeCell>,
) : NekoItem