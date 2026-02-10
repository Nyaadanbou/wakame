package cc.mewcraft.wakame.integration.towny

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * 代表一个组织: [Town] 或 [Nation].
 *
 * 共享了 [Town] 和 [Nation] 都有的 property 和 function.
 */
interface Government {

    /**
     * 返回该组织的名字.
     */
    val name: Component

    /**
     * 返回该组织的公告板, 每个元素代表一行.
     * 公告板会展示在各个列表里, 可由玩家自行调整.
     */
    var board: List<String>

    /**
     * 返回该组织的银行余额.
     */
    val balance: Double

    /**
     * 返回该组织是否可以展示在列表上.
     */
    val canShow: Boolean

    /**
     * 往该组织的银行里存钱.
     */
    fun withdraw(amount: Double)

    /**
     * 从该组织的银行里取钱.
     */
    fun deposit(amount: Double)

    /**
     * 传送玩家到该组织的中心.
     */
    fun teleport(player: Player)

    // Market Network

    /**
     * 标记该组织已加入商铺网络.
     */
    fun joinsMarketNetwork()

    /**
     * 标记该组织已经离开商铺网络.
     */
    fun leavesMarketNetwork()

    /**
     * 返回该组织是否已经加入商铺网络.
     */
    fun hasJoinedMarketNetwork(): Boolean

    /**
     * 标记该组织已经支付商铺网络税.
     */
    fun paysMarketNetworkTax()

    /**
     * 返回该组织是否已经支付商铺网络税.
     */
    fun hasPaidMarketNetworkTax(): Boolean
}
