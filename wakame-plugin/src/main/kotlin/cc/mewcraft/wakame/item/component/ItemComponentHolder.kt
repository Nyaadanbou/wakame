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
     * 用于获取 vanilla 物品组件的信息.
     */
    val item: ItemStack

    /**
     * 用于获取其他的 wakame 物品组件的信息.
     *
     * ## 警告!!!
     * 禁止利用该实例获取当前组件, 否则会无限递归引起爆栈.
     */
    val components: ItemComponentMap

    /**
     * 用于检查组件的 NBT 标签是否存在.
     *
     * 该函数的 [id] 将作为索引从 `components` 中获取相应的 [CompoundTag].
     *
     * ## NBT 结构: `components`
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
     * 用于获取组件的 NBT 标签 (如果有).
     *
     * 该函数的 [id] 将作为索引从 `components` 中获取相应的 [CompoundTag].
     */
    fun getTag(id: String): CompoundTag?

    /**
     * 用于获取组件的 NBT 标签.
     *
     * 该函数的 [id] 将作为索引从 `components` 中获取相应的 [CompoundTag].
     */
    fun getTagOrCreate(id: String): CompoundTag

    /**
     * 用于写入组件的 NBT 标签.
     *
     * 该函数的 [id] 将作为索引在 `components` 中添加相应的 [CompoundTag].
     */
    fun putTag(id: String)

    /**
     * 用于移除组件的 NBT 标签.
     *
     * 该函数的 [id] 将作为索引从 `components` 中移除相应的 [CompoundTag].
     */
    fun removeTag(id: String)

    companion object {
        fun create(item: ItemStack, compound: CompoundTag, components: ItemComponentMap): ItemComponentHolder {
            return Impl(item, components, compound)
        }
    }

    private class Impl(
        override val item: ItemStack,
        override val components: ItemComponentMap,
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

        override fun putTag(id: String) {
            compound.put(id, CompoundTag.create())
        }

        override fun removeTag(id: String) {
            compound.remove(id)
        }
    }
}