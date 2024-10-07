package cc.mewcraft.wakame.item.component

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.component.ItemComponentMap.Companion.TAG_COMPONENTS
import cc.mewcraft.wakame.util.editNyaTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import cc.mewcraft.wakame.util.unsafeNyaTag
import org.bukkit.inventory.ItemStack

/**
 * 代表一个持有了萌芽物品组件的对象.
 *
 * ## 容器的用途
 *
 * - 有些物品组件是原版物品组件的代理, 这些组件就需要访问 *[item]*.
 * - 有些物品组件是我们原创的, 这些组件就需要访问 *萌芽 NBT*.
 * - 有些物品组件比较特殊, 需要访问 *其他组件*.
 * - 有些物品组件需要以上 *所有信息*.
 *
 * ## NBT 结构
 *
 * ```NBT
 * // 这一级是包含所有物品组件的 NBT 结构,
 * // 本函数所返回的 NBT 结构都是这个结构
 * // 之下的信息.
 * Compound('components'):
 *
 *     // 这一级是包含单个物品组件的最小 NBT 结构,
 *     // 这个结构的 key 对应本函数参数中的 id.
 *     Compound('elements'):
 *
 *         // 这一级是组件内部的具体数据
 *         ...
 *
 *     Compound('kizamiz'):
 *         ...
 *
 *     Compound('cells'):
 *         ...
 * ```
 */
sealed interface ItemComponentHolder {

    companion object {
        /**
         * 创建一个 [ItemComponentHolder]. 仅供内部使用.
         */
        internal fun create(item: ItemStack): ItemComponentHolder {
            return ItemComponentHolderImpl(item)
        }
    }

    /**
     * 所封装的 [ItemStack]. 可用于获取任意原版物品组件的信息.
     */
    val item: ItemStack

    //<editor-fold desc="操作当前组件的 NBT">
    /**
     * 用于检查指定组件的 NBT 标签是否存在.
     */
    fun hasTag(id: String): Boolean

    /**
     * 获取指定组件的 NBT 标签.
     * 如果不存在则返回 `null`.
     *
     * **警告!!!**
     * 禁止修改返回的对象, 该函数仅用于读取.
     * 所有修改操作必须使用 [editTag] 函数.
     *
     * @param id 指定组件的唯一标识
     * @return 指定组件的 NBT 标签
     */
    fun getTag(id: String): CompoundTag?

    /**
     * 修改指定组件的 NBT 标签.
     * 如果不存在则创建一个新的.
     *
     * @param id 指定组件的唯一标识
     * @param edit 修改 NBT 标签的函数
     */
    fun editTag(id: String, edit: (CompoundTag) -> Unit = {})

    /**
     * 移除指定组件的 NBT 标签.
     *
     * @param id 指定组件的唯一标识
     */
    fun removeTag(id: String)
    //</editor-fold>

    //<editor-fold desc="操作萌芽的物品组件">

    //
    // 警告!!!
    // 禁止使用以下函数操作*当前组件*,
    // 否则会引发无限递归最终导致爆栈.
    //

    /**
     * 检查指定的物品组件是否存在.
     */
    fun hasData(type: ItemComponentType<*>): Boolean

    /**
     * 获取指定的物品组件.
     */
    fun <T> getData(type: ItemComponentType<T>): T?

    /**
     * 写入指定的物品组件.
     */
    fun <T> setData(type: ItemComponentType<T>, data: T)

    /**
     * 移除指定的物品组件.
     */
    fun unsetData(type: ItemComponentType<*>)
    //</editor-fold>
}


/* Implementations */


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
        val map = ItemComponentMap.wrapStack(item) // QUESTION: will it be allocated on the stack?
        val has = map.has(type)
        return has
    }

    override fun <T> getData(type: ItemComponentType<T>): T? {
        val map = ItemComponentMap.wrapStack(item)
        val data = map.get(type)
        return data
    }

    override fun <T> setData(type: ItemComponentType<T>, data: T) {
        val map = ItemComponentMap.wrapStack(item)
        map.set(type, data)
    }

    override fun unsetData(type: ItemComponentType<*>) {
        val map = ItemComponentMap.wrapStack(item)
        map.unset(type)
    }
}
