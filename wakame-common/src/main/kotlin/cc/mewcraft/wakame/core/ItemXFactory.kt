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
    val isValid: Boolean

    /**
     * 通过*通用物品标识*构建该物品库插件对应的 [ItemX].
     * 构建失败则返回空.
     */
    fun byUid(identifier: String): ItemX? {
        val split = identifier.split(":")
        if (split.size != 2) return null
        return byUid(split[0], split[1])
    }

    /**
     * 通过*通用物品标识*构建该物品库插件对应的 [ItemX].
     * 构建失败则返回空.
     */
    fun byUid(plugin: String, identifier: String): ItemX?

    /**
     * 通过 [ItemStack] 构建该物品库插件对应的 [ItemX].
     * 构建失败则返回空.
     */
    fun byItemStack(itemStack: ItemStack): ItemX?

}