package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.adventure.key.Keyed
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.stream.Stream


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
sealed interface ItemX : Keyed, Examinable {

    /**
     * 该通用物品所属的插件的名字, 必须符合 [net.kyori.adventure.key.KeyPattern.Namespace].
     */
    val plugin: String

    /**
     * 该通用物品在其所属插件的唯一标识, 必须符合 [net.kyori.adventure.key.KeyPattern.Value].
     */
    val identifier: String

    /**
     * 生成该物品的通用物品标识.
     */
    fun uid(): String {
        return "$plugin:$identifier"
    }

    /**
     * 该物品是否有效.
     *
     * 当该函数返回 `true` 时, 以下函数都可以正常工作:
     * - [matches]
     * - [createItemStack]
     * - [displayName]
     */
    fun valid(): Boolean

    // 开发日记: 2024/8/19 芙兰
    // 相比于先将 ItemStack 转成 ItemX 再 equals 进行判等,
    // 使用 matches 方法去匹配玩家背包中的物品会更快.
    // 毕竟 ItemStack 转成 ItemX 是要对每一个插件的构造器逐步尝试的.
    /**
     * 判断传入的 [itemStack] 是否与该物品匹配.
     */
    fun matches(itemStack: ItemStack): Boolean

    /**
     * 创建一个 [ItemStack], 默认数量为 1, 可以基于特定玩家生成.
     */
    fun createItemStack(amount: Int = 1, player: Player? = null): ItemStack?

    /**
     * 该物品的展示名字. 字符串为 MiniMessage string, 用作展示给玩家.
     */
    fun displayName(): String
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

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("plugin", plugin),
        ExaminableProperty.of("identifier", identifier),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        return other is ItemX &&
                plugin == other.plugin &&
                identifier == other.identifier
    }

    override fun hashCode(): Int {
        return plugin.hashCode() + 31 * identifier.hashCode()
    }

    override fun toString(): String {
        return examine(StringExaminer.simpleEscaping())
    }
}