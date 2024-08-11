package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.adventure.key.Keyed
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


/**
 * 代表一个通用的物品. 可以是萌芽物品、原版物品、第三方插件物品.
 *
 * ## 通用物品标识
 * 通用物品标识是一个字符串, 由插件名和物品名组成, 用 `:` 分隔.
 * 设计上, 用户给定一个通用物品标识, 就可以唯一确定一个物品.
 * 因此通用物品标识的选择应该尽量避免冲突, 且需保证唯一性.
 *
 * 插件名和物品名的规则请参考 [plugin] 和 [identifier].
 *
 * ## 实现指南
 * 用户应该直接实现 [ItemXAbstract].
 */
sealed interface ItemX : Keyed {

    /**
     * 该通用物品所属的插件的名字, 必须符合 [net.kyori.adventure.key.KeyPattern.Namespace].
     */
    val plugin: String

    /**
     * 该通用物品在其所属插件的唯一标识, , 必须符合 [net.kyori.adventure.key.KeyPattern.Value].
     */
    val identifier: String

    /**
     * 生成该物品的通用物品标识.
     */
    fun uid(): String {
        return "$plugin:$identifier"
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

/**
 * [ItemX] 的基本实现.
 */
abstract class ItemXAbstract(
    override val plugin: String,
    override val identifier: String,
) : ItemX {
    override val key: Key = Key.key(plugin, identifier)

    constructor(uid: String) : this(
        uid.substringBefore(':'), uid.substringAfter(':')
    )
}