package cc.mewcraft.wakame.item.component

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import org.bukkit.inventory.ItemStack

/**
 * 代表一个储存了(wakame)物品组件的容器.
 *
 * 目前容器主要分为以下几类:
 *
 * - 有些物品组件是原版物品组件的代理, 这些组件就需要访问 [ItemStack].
 * - 有些物品组件是我们原创的, 这些组件就需要访问 NBT.
 * - 有些物品组件比较特殊, 需要访问其他的组件.
 * - 有些物品组件需要以上所有信息.
 */
sealed interface ItemComponentHolder {

    /**
     * 用于获取任意原版物品组件的信息.
     */
    val item: ItemStack

    //<editor-fold desc="操作当前组件的 NBT">
    // 统一说明:
    // 以下函数的 [id] 参数都将作为索引从 `components` 中获取相应的 [CompoundTag].

    /**
     * 用于检查指定组件的 NBT 标签是否存在.
     *
     * ## 关于 NBT 结构的统一说明
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
     *
     *         ...
     *
     *     Compound('cells'):
     *
     *         ...
     * ```
     */
    fun hasTag(id: String): Boolean

    /**
     * 获取指定组件的 NBT 标签 (如果有).
     *
     * @return 指定的 NBT 标签
     */
    fun getTag(id: String): CompoundTag?

    /**
     * 获取指定组件的 NBT 标签.
     *
     * @return 如果指定的 NBT 标签已经存在则返回已存在的; 如果不存在则返回新创建的
     */
    fun getTagOrCreate(id: String): CompoundTag

    /**
     * 写入指定组件的 NBT 标签.
     *
     * @return 原本已经存在的 NBT 标签; 如果原本不存在则返回 `null`
     */
    fun putTag(id: String): CompoundTag?

    /**
     * 移除指定组件的 NBT 标签.
     */
    fun removeTag(id: String)
    //</editor-fold>

    //<editor-fold desc="操作萌芽的物品组件">
    /**
     * 检查指定的萌芽物品组件是否存在.
     *
     * ## 警告!!!
     * 禁止使用该函数操作当前组件, 否则会无限递归引起爆栈.
     */
    fun hasData(type: ItemComponentType<*>): Boolean

    /**
     * 获取指定的萌芽物品组件.
     *
     * ## 警告!!!
     * 禁止使用该函数操作当前组件, 否则会无限递归引起爆栈.
     */
    fun <T> getData(type: ItemComponentType<T>): T?

    /**
     * 设置指定的萌芽物品组件.
     *
     * ## 警告!!!
     * 禁止使用该函数操作当前组件, 否则会无限递归引起爆栈.
     */
    fun <T> setData(type: ItemComponentType<T>, data: T)

    /**
     * 移除指定的萌芽物品组件.
     *
     * ## 警告!!!
     * 禁止使用该函数操作当前组件, 否则会无限递归引起爆栈.
     */
    fun unsetData(type: ItemComponentType<*>)
    //</editor-fold>

    //<editor-fold desc="操作原版的物品组件">
    /**
     * 检查指定的原版物品组件是否存在.
     */
    fun hasData(type: DataComponentType): Boolean

    /**
     * 获取指定的原版物品组件.
     */
    fun <T> getData(type: DataComponentType.Valued<T>): T?

    /**
     * 获取指定的原版物品组件, 或返回默认值.
     */
    fun <T> getDataOrDefault(type: DataComponentType.Valued<T>, fallback: T): T

    /**
     * 检查指定的原版物品组件是否已被重写.
     */
    fun isOverridden(type: DataComponentType): Boolean

    /**
     * 获取该物品上所有的原版组件信息.
     */
    fun getDataTypes(): Set<DataComponentType>

    /**
     * 设置指定的原版物品组件.
     */
    fun <T> setData(type: DataComponentType.NonValued)

    /**
     * 设置指定的原版物品组件.
     */
    fun <T> setData(type: DataComponentType.Valued<T>, data: T)

    /**
     * 移除指定的原版物品组件.
     */
    fun unsetData(type: DataComponentType)

    /**
     * 重置指定的原版物品组件.
     */
    fun resetData(type: DataComponentType)
    //</editor-fold>

    companion object {
        /**
         * 创建一个 [ItemComponentHolder]. 仅供内部使用.
         */
        internal fun create(compound: CompoundTag, item: ItemStack, components: ItemComponentMap): ItemComponentHolder {
            return Impl(item, components, compound)
        }
    }

    private class Impl(
        override val item: ItemStack,
        private val components: ItemComponentMap,
        private val compound: CompoundTag,
    ) : ItemComponentHolder {
        override fun hasTag(id: String): Boolean {
            return compound.contains(id, TagType.COMPOUND)
        }

        override fun getTag(id: String): CompoundTag? {
            return compound.getCompoundOrNull(id)
        }

        override fun getTagOrCreate(id: String): CompoundTag {
            return compound.getOrPut(id, CompoundTag::create)
        }

        override fun putTag(id: String): CompoundTag? {
            return compound.put(id, CompoundTag.create()) as CompoundTag?
        }

        override fun removeTag(id: String) {
            compound.remove(id)
        }

        override fun hasData(type: ItemComponentType<*>): Boolean {
            return components.has(type)
        }

        override fun hasData(type: DataComponentType): Boolean {
            throw UnsupportedOperationException()
        }

        override fun <T> getData(type: ItemComponentType<T>): T? {
            return components.get(type)
        }

        override fun <T> getData(type: DataComponentType.Valued<T>): T? {
            throw UnsupportedOperationException()
        }

        override fun <T> setData(type: ItemComponentType<T>, data: T) {
            components.set(type, data)
        }

        override fun <T> setData(type: DataComponentType.NonValued) {
            throw UnsupportedOperationException()
        }

        override fun <T> setData(type: DataComponentType.Valued<T>, data: T) {
            throw UnsupportedOperationException()
        }

        override fun unsetData(type: ItemComponentType<*>) {
            components.unset(type)
        }

        override fun unsetData(type: DataComponentType) {
            throw UnsupportedOperationException()
        }

        override fun <T> getDataOrDefault(type: DataComponentType.Valued<T>, fallback: T): T {
            throw UnsupportedOperationException()
        }

        override fun isOverridden(type: DataComponentType): Boolean {
            throw UnsupportedOperationException()
        }

        override fun getDataTypes(): Set<DataComponentType> {
            throw UnsupportedOperationException()
        }

        override fun resetData(type: DataComponentType) {
            throw UnsupportedOperationException()
        }
    }
}

// TODO 2024/6/30 等 Paper 的 DataComponent API 推出以后, 添加相应的添加/获取/移除 vanilla 物品组件的函数
@Deprecated("DataComponent API 推出后换掉")
interface DataComponentType {
    interface Valued<T> : DataComponentType
    interface NonValued : DataComponentType
}
