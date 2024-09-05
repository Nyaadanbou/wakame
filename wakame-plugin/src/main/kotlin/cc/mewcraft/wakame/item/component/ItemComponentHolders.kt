package cc.mewcraft.wakame.item.component

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.component.ItemComponentMap.Companion.TAG_COMPONENTS
import cc.mewcraft.wakame.util.editNyaTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import cc.mewcraft.wakame.util.unsafeNyaTag
import org.bukkit.inventory.ItemStack

internal object ItemComponentHolders {
    /**
     * 创建一个 [ItemComponentHolder]. 仅供内部使用.
     */
    fun create(item: ItemStack): ItemComponentHolder {
        return ItemComponentHolderImpl(item)
    }
}


/* Privates */


private class ItemComponentHolderImpl(
    override val item: ItemStack,
) : ItemComponentHolder {
    /**
     * 从萌芽的根 NBT 标签中获取键为 [TAG_COMPONENTS] 的标签.
     * 该函数返回的是直接引用, 禁止修改其任何状态!
     */
    private fun getUnsafeComponents(): CompoundTag? {
        val nyaTag = item.unsafeNyaTag ?: return null // 并发环境下可能为 null
        val components = nyaTag.getCompoundOrNull(TAG_COMPONENTS)
        return components
    }

    override fun hasTag(id: String): Boolean {
        val components = getUnsafeComponents() ?: return false
        val contains = components.contains(id)
        return contains
    }

    override fun getTag(id: String): CompoundTag? {
        val components = getUnsafeComponents() ?: return null
        val compound = components.getCompoundOrNull(id)
        return compound
    }

    override fun editTag(id: String, edit: (CompoundTag) -> Unit) {
        item.editNyaTag { tag ->
            // 获取存放所有组件的 NBT 标签
            val components = tag.getOrPut(TAG_COMPONENTS, CompoundTag::create)
            // 获取 id 对应的组件 NBT 标签
            val compound = components.getOrPut(id, CompoundTag::create)
            // 修改这个 NBT 标签
            edit(compound)
        }
    }

    override fun removeTag(id: String) {
        item.editNyaTag {
            // 获取存放所有组件的 NBT 标签
            val components = it.getCompoundOrNull(TAG_COMPONENTS) ?: return@editNyaTag
            // 移除 id 对应的组件 NBT 标签
            components.remove(id)
        }
    }

    override fun hasData(type: ItemComponentType<*>): Boolean {
        val map = ItemComponentMaps.wrapStack(item) // QUESTION: will it be allocated on the stack?
        val has = map.has(type)
        return has
    }

    override fun <T> getData(type: ItemComponentType<T>): T? {
        val map = ItemComponentMaps.wrapStack(item)
        val data = map.get(type)
        return data
    }

    override fun <T> setData(type: ItemComponentType<T>, data: T) {
        val map = ItemComponentMaps.wrapStack(item)
        map.set(type, data)
    }

    override fun unsetData(type: ItemComponentType<*>) {
        val map = ItemComponentMaps.wrapStack(item)
        map.unset(type)
    }
}
