package cc.mewcraft.wakame.core

import org.bukkit.inventory.ItemStack


/**
 * 封装了 [ItemX] 的构建逻辑.
 */
interface ItemXFactory {

    /**
     * 所属的插件的名字.
     */
    val plugin: String

    /**
     * 该物品库插件是否已经完成加载.
     */
    val loaded: Boolean

    /**
     * 通过 *通用物品标识* 构建该物品库系统对应的 [ItemX].
     * 类似 [create], 只不过调用方不需要手动拆分 [uid].
     * 构建失败则返回 `null`.
     */
    fun create(uid: String): ItemX? {
        val split = uid.split(":")
        if (split.size != 2) return null
        return create(split[0], split[1])
    }

    /**
     * 通过 *通用物品标识* 构建该物品库系统对应的 [ItemX].
     * 构建失败则返回 `null`.
     */
    fun create(plugin: String, identifier: String): ItemX?

    /**
     * 通过 *已有的物品堆叠* 构建该物品堆叠对应的 [ItemX].
     * 构建失败则返回 `null`.
     */
    fun create(itemStack: ItemStack): ItemX?

}