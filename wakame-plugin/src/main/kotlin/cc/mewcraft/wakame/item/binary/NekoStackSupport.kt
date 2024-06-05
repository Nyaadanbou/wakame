package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.BaseBinaryKeys
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessorImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorImpl
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessorImpl
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Common code shared by all [NekoStack] implementations.
 */
internal interface NekoStackBase : NekoStack {
    override val schema: NekoItem
        get() = ItemRegistry.INSTANCES[key]

    override var key: Key
        get() = Key(namespace, path)
        set(value) {
            namespace = value.namespace()
            path = value.value()
        }

    override var namespace: String
        get() = tags.getString(BaseBinaryKeys.NAMESPACE)
        set(value) = tags.putString(BaseBinaryKeys.NAMESPACE, value)

    override var path: String
        get() = tags.getString(BaseBinaryKeys.PATH)
        set(value) = tags.putString(BaseBinaryKeys.PATH, value)

    override var variant: Int
        get() = tags.getInt(BaseBinaryKeys.VARIANT)
        set(value) = tags.putInt(BaseBinaryKeys.VARIANT, value)

    override val uuid: UUID
        get() = ItemRegistry.INSTANCES[key].uuid

    override val slot: ItemSlot
        get() = ItemRegistry.INSTANCES[key].slot

    override val cell: ItemCellAccessor
        get() = ItemCellAccessorImpl(this)

    override val meta: ItemMetaAccessor
        get() = ItemMetaAccessorImpl(this)

    override val statistics: ItemStatisticsAccessor
        get() = ItemStatisticsAccessorImpl(this)

    override val behaviors: List<ItemBehavior>
        get() = schema.behaviors

    override fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T {
        return getBehaviorOrNull(behaviorClass)
            ?: throw IllegalStateException("Item $key does not have a behavior of type ${behaviorClass.simpleName}")
    }
}