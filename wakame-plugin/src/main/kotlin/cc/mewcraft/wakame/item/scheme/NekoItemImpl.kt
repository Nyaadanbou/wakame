package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.scheme.behavior.BehaviorRegistry
import cc.mewcraft.wakame.item.scheme.behavior.ItemBehavior
import cc.mewcraft.wakame.item.scheme.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta
import cc.mewcraft.wakame.provider.ConfigProvider
import cc.mewcraft.wakame.provider.node
import com.google.common.collect.ClassToInstanceMap
import net.kyori.adventure.key.Key
import java.util.*

internal data class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val config: ConfigProvider,
    override val material: Key,
    override val effectiveSlot: EffectiveSlot,
    override val meta: ClassToInstanceMap<SchemeItemMeta<*>>,
    override val cell: Map<String, SchemeCell>,
    private val behaviorHolders: List<String>,
) : NekoItem {
    override val behaviors: List<ItemBehavior> = behaviorHolders
        .map { BehaviorRegistry.getOrThrow(it) to it }
        .map { (behavior, behaviorName) ->
            when (behavior) {
                is ItemBehavior -> behavior
                is ItemBehaviorFactory<*> -> behavior.create(this, config.node("behaviors", behaviorName))
            }
        }
}