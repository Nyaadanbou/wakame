package cc.mewcraft.wakame.integration.towny

import java.util.*

/**
 * 用来获取服务器上所有 [Town] 和 [Nation] 的接口.
 */
interface TownyLocal {

    /**
     * 获取服务器上的所有 [Town].
     */
    fun getTowns(): Collection<Town>

    /**
     * 返回服务器上的所有 [Nation].
     */
    fun getNations(): Collection<Nation>

    /**
     * 返回玩家所属的 [Town], 如果没有则返回 null.
     */
    fun getTown(playerId: UUID): Town?

    /**
     * 返回玩家所属的 [Nation], 如果没有则返回 null.
     */
    fun getNation(playerId: UUID): Nation?

    /**
     * 返回是否为镇长.
     */
    fun isMayor(playerId: UUID): Boolean

    /**
     * 返回是否为国王.
     */
    fun isKing(playerId: UUID): Boolean

    // Market Network

    /**
     * 标记 [government] 已加入市场网络.
     */
    fun joinsMarketNetwork(government: Government)

    /**
     * 标记 [government] 已经离开市场网络.
     */
    fun leavesMarketNetwork(government: Government)

    /**
     * 返回 [government] 是否已经加入市场网络.
     */
    fun hasJoinedMarketNetwork(government: Government): Boolean

    /**
     * 标记 [government] 已经支付市场网络税.
     */
    fun paysMarketNetworkTax(government: Government)

    /**
     * 返回 [government] 是否已经支付市场网络税.
     */
    fun hasPaidMarketNetworkTax(government: Government): Boolean


    companion object Impl : TownyLocal {

        private var implementation: TownyLocal = object : TownyLocal {
            override fun getTowns(): Collection<Town> = emptyList()
            override fun getNations(): Collection<Nation> = emptyList()
            override fun getTown(playerId: UUID): Town? = null
            override fun getNation(playerId: UUID): Nation? = null
            override fun isMayor(playerId: UUID): Boolean = false
            override fun isKing(playerId: UUID): Boolean = false
            override fun joinsMarketNetwork(government: Government) = Unit
            override fun leavesMarketNetwork(government: Government) = Unit
            override fun hasJoinedMarketNetwork(government: Government): Boolean = false
            override fun paysMarketNetworkTax(government: Government) = Unit
            override fun hasPaidMarketNetworkTax(government: Government): Boolean = false
        }

        fun setImplementation(provider: TownyLocal) {
            implementation = provider
        }

        override fun getTowns(): Collection<Town> = implementation.getTowns()
        override fun getNations(): Collection<Nation> = implementation.getNations()
        override fun getTown(playerId: UUID): Town? = implementation.getTown(playerId)
        override fun getNation(playerId: UUID): Nation? = implementation.getNation(playerId)
        override fun isMayor(playerId: UUID): Boolean = implementation.isMayor(playerId)
        override fun isKing(playerId: UUID): Boolean = implementation.isKing(playerId)
        override fun joinsMarketNetwork(government: Government): Unit = implementation.joinsMarketNetwork(government)
        override fun leavesMarketNetwork(government: Government): Unit = implementation.leavesMarketNetwork(government)
        override fun hasJoinedMarketNetwork(government: Government): Boolean = implementation.hasJoinedMarketNetwork(government)
        override fun paysMarketNetworkTax(government: Government) = implementation.paysMarketNetworkTax(government)
        override fun hasPaidMarketNetworkTax(government: Government): Boolean = implementation.hasPaidMarketNetworkTax(government)
    }
}