package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.item.schema.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.registry.BehaviorRegistry
import com.google.common.collect.ClassToInstanceMap
import net.kyori.adventure.key.Key
import java.util.UUID
import kotlin.reflect.KClass

internal data class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val config: ConfigProvider,
    override val material: Key,
    override val effectiveSlot: EffectiveSlot,
    private val metaMap: ClassToInstanceMap<SchemaItemMeta<*>>,
    private val cellMap: Map<String, SchemaCell>,
    private val behaviorHolders: List<String>,
) : NekoItem {
    override val meta: Set<SchemaItemMeta<*>> = metaMap.values.toSet()
    override val cell: Set<SchemaCell> = cellMap.values.toSet()

    override fun <T : SchemaItemMeta<*>> getMeta(metaClass: KClass<T>): T {
        return requireNotNull(metaMap.getInstance(metaClass.java)) { "Can't find schema item meta '$metaClass' for item '$key'" }
    }

    override fun getCell(id: String): SchemaCell? {
        return cellMap[id]
    }

    override val behaviors: List<ItemBehavior> = behaviorHolders
        .map { BehaviorRegistry.INSTANCES[it] to it }
        .map { (behavior, behaviorName) ->
            when (behavior) {
                is ItemBehavior -> behavior
                is ItemBehaviorFactory<*> -> behavior.create(this, config.node("behaviors", behaviorName))
            }
        }

    override fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T {
        return getBehaviorOrNull(behaviorClass) ?: throw IllegalStateException("Item $key does not have a behavior of type ${behaviorClass.simpleName}")
    }

    // 必须最后执行验证，以保证所有 member properties 已经初始化
    init {
        NekoItemValidator.chain(
            BehaviorValidator(),
        ).validate(NekoItemValidator.Args(this))
    }
}