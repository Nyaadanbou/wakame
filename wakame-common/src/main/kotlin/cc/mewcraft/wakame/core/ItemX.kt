package cc.mewcraft.wakame.core

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


/**
 * 代表各种插件物品库的物品.
 * 可能是wakame物品、原版物品、第三方插件物品.
 */
interface ItemX {

    /**
     * 该物品库插件的id(小写英文字母).
     */
    val plugin: String

    /**
     * 该物品的id(小写英文字母).
     */
    val itemId: String

    /**
     * 生成该物品的通用物品标识.
     */
    fun asReference(): String {
        return "$plugin:$itemId"
    }

    /**
     * 创建一个 [ItemStack].
     */
    fun createItemStack(): ItemStack?

    /**
     * 根据玩家创建一个 [ItemStack].
     */
    fun createItemStack(player: Player): ItemStack?

    /**
     * 判断传入的 [itemStack] 是否与该物品匹配.
     */
    fun matches(itemStack: ItemStack): Boolean
}
