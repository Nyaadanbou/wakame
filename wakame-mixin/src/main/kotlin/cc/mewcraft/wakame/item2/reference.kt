@file:JvmName("ItemReferenceApi")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// FIXME #350:
//  验证机制:
//    - 在配置文件加载阶段: 创建的 ItemRef 可以验证有效性. 当全部的 ItemRef 创建完毕后,
//      如果发现了无效 ItemRef 则强制关服, 要求管理员修复无效引用再启动服务端.
//    - 在游戏运行时: 创建的 ItemRef 除了专门处理无效的情况, 没有其他很好的办法.
//      区别只有处理的有多好看, 有多方便.

// ------------
// 物品引用
//
// 用于统一 指定/查询/比较 *物品类型* 的方式
// ------------

/**
 * 本接口代表一个*物品类型*的引用. 引用的形式为 [Identifier].
 *
 * 所有属性和函数的行为, 均只考虑对于各个物品系统内部而言的物品类型, 不会判断物品堆叠上产生的任何数据.
 *
 * 例如: 在 Minecraft 的物品系统下, 拥有不同附魔的游戏原版钻石剑将全部算作是同一个 [ItemRef].
 * 在 Koish 的物品系统物品下, 等级不一样但物品ID一样的冒险者之剑将全部算作是同一个 [ItemRef].
 */
interface ItemRef {

    companion object {

        /**
         * 创建一个新的 [ItemRef]. 实现上会尽可能的复用已有的对象.
         *
         * @param phase 当前代码所处的运行阶段
         */
        fun create(id: Identifier, phase: Phase): ItemRef {
            TODO("#350")
        }

        /**
         * 传入的 [Identifier] 必须可以被当前系统理解.
         */
        fun of(id: Identifier): ItemRef {
            TODO("#350")
        }

        /**
         * 对于任何 [ItemStack] 永远存在有效的 [ItemRef].
         */
        fun of(stack: ItemStack): ItemRef {
            TODO("#350")
        }

    }

    /**
     * 用来区分不同 [ItemRef] 的唯一标识.
     * 命名空间用来区分来自不同系统的物品类型.
     */
    val id: Identifier

    /**
     * 获取一个表示该物品类型的物品名字. 该名字可以展示给玩家, 并被玩家所理解.
     */
    val name: Component

    /**
     * 判断传入的 [Identifier] 是否和当前 [ItemRef] 匹配.
     */
    fun matches(id: Identifier): Boolean

    /**
     * 判断传入的 [ItemRef] 是否和当前 [ItemRef] 匹配.
     */
    fun matches(ref: ItemRef): Boolean

    /**
     * 判断传入的 [ItemStack] 是否和当前 [ItemRef] 匹配.
     */
    fun matches(stack: ItemStack): Boolean

    /**
     * 从该 [ItemRef] 创建一个 [ItemStack].
     *
     * @param amount 该物品堆叠的数量, 受游戏本身限制
     * @param player 接收该物品堆叠的玩家. 传入此参数可以让某些系统基于该玩家来生成物品
     * @return 新生成的 [ItemStack]
     */
    fun createItemStack(amount: Int = 1, player: Player? = null): ItemStack

    enum class Phase {
        /**
         * 阶段: 配置文件加载.
         */
        CONFIGURATION,

        /**
         * 阶段: 游戏运行时.
         */
        PLAY,
    }

}

// ------------
// 管理 ItemRef 的对象池
// ------------

object ItemRefManager {
    private val POOL = HashMap<Identifier, ItemRef>()
}

// ------------
// ItemRefHandler
// 实现该接口以支持处理来自特定系统的物品类型
// ------------

/**
 * @param T 该物品系统之下的内部物品类型
 */
private interface ItemRefHandler<T> {

    val systemName: String

    // 判断该系统是否可以处理传入的 id
    // 实现上只需要判断 namespace, 无需检查整个 id 是否指向一个系统内有效的物品类型
    fun canHandle(id: Identifier): Boolean

    // 实现应该假设 id 永远是合法 id
    // 对于不合法的 id 可以直接抛出异常 (bug)
    fun getName(id: Identifier): Component

    // 实现应该假设 xId 永远是合法 id, 但对于 yId 可以假设其为非法 id
    // 对于不合法的 xId 可以直接抛出异常 (bug)
    fun matches(xId: Identifier, yId: Identifier): Boolean {
        return xId == yId
    }

    // ItemRef 只是个形如 x:y 的 id
    // 所以同上, 只不过是对于 ItemRef
    fun matches(xRef: ItemRef, yRef: ItemRef): Boolean {
        return xRef == yRef
    }

    // 同上, 只不过是对于 ItemStack
    fun matches(xStack: ItemStack, yStack: ItemStack): Boolean

    // 实现应该假设 id 永远是合法 id
    // 对于不合法的 id 可以直接抛出异常 (bug)
    fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack

    // 在该系统下获取 id 对应的物品类型 T
    // 对于找不到的物品类型该函数会直接抛异常 (bug)
    fun getInternalType(id: Identifier): T

    // 方便函数
    fun throwItemTypeNotFound(id: Identifier): Nothing {
        error("Cannot find a valid $systemName item type by: $id. This is a bug!")
    }

}

private data object MinecraftItemRefHandler : ItemRefHandler<Material> {

    override val systemName: String = "Minecraft"

    override fun canHandle(id: Identifier): Boolean {
        return id.namespace() == MINECRAFT_NAMESPACE
    }

    override fun getName(id: Identifier): Component {
        val type = getInternalType(id)
        return Component.translatable(type)
    }

    override fun matches(xStack: ItemStack, yStack: ItemStack): Boolean {
        val xId = xStack.type.key
        val yId = yStack.type.key
        return matches(xId, yId)
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack {
        val type = getInternalType(id)
        return ItemStack(type).apply { this.amount = amount }
    }

    override fun getInternalType(id: Identifier): Material {
        return Material.matchMaterial(id.asString()) ?: throwItemTypeNotFound(id)
    }

}

private data object KoishItemRefHandler : ItemRefHandler<KoishItem> {

    override val systemName: String = "Koish"

    override fun canHandle(id: Identifier): Boolean {
        return id.namespace() == KOISH_NAMESPACE
    }

    override fun getName(id: Identifier): Component {
        val type = getInternalType(id)
        return type.name
    }

    override fun matches(xStack: ItemStack, yStack: ItemStack): Boolean {
        val xId = xStack.typeId
        val yId = yStack.typeId
        return matches(xId, yId)
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack {
        val type = getInternalType(id)
        val item = KoishStackGenerator.generate(type, Context())
        return item
    }

    override fun getInternalType(id: Identifier): KoishItem {
        return KoishRegistries2.ITEM[id] ?: throwItemTypeNotFound(id)
    }

}