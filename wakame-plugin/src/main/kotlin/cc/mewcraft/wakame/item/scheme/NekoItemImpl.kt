package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.scheme.behavior.ItemBehavior
import cc.mewcraft.wakame.item.scheme.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.scheme.behavior.ItemBehaviorHolder
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta
import com.google.common.collect.ClassToInstanceMap
import net.kyori.adventure.key.Key
import xyz.xenondevs.nova.data.config.ConfigProvider
import java.util.UUID

internal data class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val config: ConfigProvider,
    override val material: Key,
    override val effectiveSlot: EffectiveSlot,
    override val meta: ClassToInstanceMap<SchemeItemMeta<*>>,
    override val cell: Map<String, SchemeCell>,
    val behaviorHolders: List<ItemBehaviorHolder>,
) : NekoItem {
    override val behaviors: List<ItemBehavior>
        get() = behaviorHolders.map {
            when (val behavior = it) {
                is ItemBehavior -> behavior
                is ItemBehaviorFactory<*> -> behavior.create(this)
            }
        }
}