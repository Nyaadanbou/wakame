package cc.mewcraft.wakame.integration.towny

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

interface Town : Government, MenuListEntry, MarketNetworkEntity

interface Nation : Government, MenuListEntry, MarketNetworkEntity

enum class GovernmentType {
    TOWN, NATION
}

interface Government {
    /**
     * 返回该实体的银行余额.
     */
    val balance: Double

    /**
     * 往该实体的银行里存钱.
     */
    fun withdraw(amount: Double)

    /**
     * 从该实体的银行里取钱.
     */
    fun deposit(amount: Double)
}

interface MenuListEntry {
    /**
     * 返回该实体的名字.
     */
    val name: Component

    /**
     * 返回该实体是否可以展示在列表上.
     */
    val canShow: Boolean

    /**
     * 返回该实体的公告板, 每个字符串代表一行.
     * 公告板会展示在各个列表里, 可由玩家自行调整.
     */
    var board: List<String>

    /**
     * 将玩家传送到该重生点.
     */
    fun teleport(player: Player)
}

interface MarketNetworkEntity {
    /**
     * 标记该实体已加入商铺网络.
     */
    fun joinsMarketNetwork()

    /**
     * 标记该实体已经离开商铺网络.
     */
    fun leavesMarketNetwork()

    /**
     * 返回该实体是否已经加入商铺网络.
     */
    fun hasJoinedMarketNetwork(): Boolean
}
