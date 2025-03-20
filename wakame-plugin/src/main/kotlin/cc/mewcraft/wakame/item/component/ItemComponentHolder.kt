package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.KoishStackImplementations
import cc.mewcraft.wakame.item.component.ItemComponentMap.Companion.COMPONENTS_FIELD
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.data.getCompoundOrNull
import cc.mewcraft.wakame.util.data.getOrPut
import cc.mewcraft.wakame.util.item.toBukkit
import net.minecraft.nbt.CompoundTag
import org.bukkit.inventory.ItemStack

/**
 * 代表一个持有了萌芽物品组件的对象.
 *
 * ## 容器的用途
 *
 * - 有些物品组件是原版物品组件的代理, 这些组件就需要访问 *[bukkitStack]*.
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
interface ItemComponentHolder {

    companion object {

        fun itemComponentHolder(itemstack: MojangStack): ItemComponentHolder {
            return ItemComponentHolderImpl(itemstack)
        }

    }

    /**
     * 底层的 [ItemStack].
     */
    val bukkitStack: ItemStack

    /**
     * 底层的 [ItemStack].
     */
    val mojangStack: MojangStack

    /**
     * 用于检查指定组件的 NBT 标签是否存在.
     */
    fun hasNbt(id: String): Boolean

    /**
     * 获取指定组件的 NBT 标签.
     * 如果不存在则返回 `null`.
     *
     * 禁止修改返回的对象, 该函数仅用于读取.
     * 所有修改操作必须使用 [editNbt] 函数.
     *
     * @param id 指定组件的唯一标识
     * @return 指定组件的 NBT 标签
     */
    fun getNbt(id: String): CompoundTag?

    /**
     * 修改指定组件的 NBT 标签.
     * 如果不存在则创建一个新的.
     *
     * @param id 指定组件的唯一标识
     * @param edit 修改 NBT 标签的函数
     */
    fun editNbt(id: String, edit: (CompoundTag) -> Unit = {})

    /**
     * 移除指定组件的 NBT 标签.
     *
     * @param id 指定组件的唯一标识
     */
    fun removeNbt(id: String)

}

private class ItemComponentHolderImpl(
    override val mojangStack: MojangStack,
) : ItemComponentHolder {

    override val bukkitStack: ItemStack
        get() = mojangStack.toBukkit()

    /**
     * 从萌芽的根 NBT 标签中获取键为 [COMPONENTS_FIELD] 的标签.
     * 该函数返回的是直接引用, 禁止修改其任何状态!
     */
    private fun getRootDataCompound(): CompoundTag? {
        // 并发环境下(网络发包时)可能为 null
        return KoishStackImplementations.getNbt(mojangStack)?.getCompoundOrNull(COMPONENTS_FIELD)
    }

    override fun hasNbt(id: String): Boolean {
        return getRootDataCompound()?.contains(id) == true
    }

    override fun getNbt(id: String): CompoundTag? {
        return getRootDataCompound()?.getCompoundOrNull(id)
    }

    override fun editNbt(id: String, edit: (CompoundTag) -> Unit) {
        KoishStackImplementations.editNbt(mojangStack) { koishCompound ->
            // 获取存放所有组件的 NBT
            val rootDataCompound = koishCompound.getOrPut(COMPONENTS_FIELD, ::CompoundTag)
            // 获取 id 对应的组件 NBT
            val dataCompound = rootDataCompound.getOrPut(id, ::CompoundTag)
            // 修改这个 NBT 标签
            edit(dataCompound)
        }
    }

    override fun removeNbt(id: String) {
        KoishStackImplementations.editNbt(mojangStack) { koishCompound ->
            koishCompound.getCompoundOrNull(COMPONENTS_FIELD)?.remove(id)
        }
    }

}