package cc.mewcraft.wakame.item.binary

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.BaseBinaryKeys
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessorImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorImpl
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessorImpl
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Contains code shared by all [NekoStack] implementations.
 */
internal interface NekoStackBase : NekoStack {
    override val schema: NekoItem
        get() = ItemRegistry.INSTANCES[key]

    override var key: Key
        get() = NekoStackImplementation.getKey(tags)!!
        set(value) = NekoStackImplementation.setKey(tags, value)

    override var namespace: String
        get() = NekoStackImplementation.getNamespace(tags)!!
        set(value) = NekoStackImplementation.setNamespace(tags, value)

    override var path: String
        get() = NekoStackImplementation.getPath(tags)!!
        set(value) = NekoStackImplementation.setPath(tags, value)

    override var variant: Int
        get() = tags.getInt(BaseBinaryKeys.VARIANT)
        set(value) = tags.putInt(BaseBinaryKeys.VARIANT, value)

    override val uuid: UUID
        get() = ItemRegistry.INSTANCES[key].uuid

    override val slot: ItemSlot
        get() = ItemRegistry.INSTANCES[key].slot

    override val components: ItemComponentMap
        get() = ItemComponentMap.wrapItem(itemStack)

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

/**
 * Some internal functions related to [NekoStack].
 */
internal object NekoStackImplementation {
    fun getNamespace(nekoCompound: CompoundTag?): String? {
        return nekoCompound?.getString(BaseBinaryKeys.NAMESPACE)
    }

    fun getPath(nekoCompound: CompoundTag?): String? {
        return nekoCompound?.getString(BaseBinaryKeys.PATH)
    }

    fun getKey(nekoCompound: CompoundTag?): Key? {
        return getNamespace(nekoCompound)?.let { namespace ->
            getPath(nekoCompound)?.let { path ->
                Key(namespace, path)
            }
        }
    }

    fun setNamespace(nekoCompound: CompoundTag?, namespace: String) {
        nekoCompound?.putString(BaseBinaryKeys.NAMESPACE, namespace)
    }

    fun setPath(nekoCompound: CompoundTag?, path: String) {
        nekoCompound?.putString(BaseBinaryKeys.PATH, path)
    }

    fun setKey(nekoCompound: CompoundTag?, key: Key) {
        setNamespace(nekoCompound, key.namespace())
        setPath(nekoCompound, key.value())
    }
}