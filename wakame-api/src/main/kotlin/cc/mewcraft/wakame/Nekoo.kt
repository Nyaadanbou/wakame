package cc.mewcraft.wakame

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * The Nekoo API.
 */
interface Nekoo {
    /**
     * 通过指定萌芽物品的 [id] 创建一个新的 [ItemStack].
     *
     * 你可以传入一个 [source], 使得生成结果基于该玩家的信息.
     */
    fun createItemStack(id: Key, source: Player? = null): ItemStack

    /**
     * @see createItemStack
     */
    fun createItemStack(namespace: String, path: String, source: Player? = null): ItemStack

    /**
     * 判断一个 [ItemStack] 是否是一个萌芽物品.
     */
    fun isNekoStack(itemStack: ItemStack): Boolean

    /**
     * 获取一个 [ItemStack] 的萌芽物品 ID. 如果不是萌芽物品, 返回 null.
     */
    fun getNekoId(itemStack: ItemStack): Key?
}