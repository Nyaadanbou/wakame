package cc.mewcraft.wakame.integration.townynetwork

import org.bukkit.entity.Player

/**
 * Towny 的跨服消息接口.
 *
 * 使用该接口来执行所有关于跨服的操作.
 */
interface TownyNetworkIntegration {

    /**
     * 将玩家 [player] 传送到指定服务器 [targetServer] 里面的(玩家所属的)城镇传送点.
     */
    suspend fun requestTeleportTown(player: Player, targetServer: String)

    /**
     * 将玩家 [player] 传送到指定服务器 [targetServer] 里面的(玩家所属的)国家传送点.
     */
    suspend fun requestTeleportNation(player: Player, targetServer: String)

    /**
     * 该伴生对象持有了 [TownyNetworkIntegration] 的当前实现.
     */
    companion object : TownyNetworkIntegration {
        private var implementation: TownyNetworkIntegration? = null

        fun setImplementation(impl: TownyNetworkIntegration) {
            this.implementation = impl
        }

        override suspend fun requestTeleportTown(player: Player, targetServer: String) {
            implementation?.requestTeleportTown(player, targetServer)
        }

        override suspend fun requestTeleportNation(player: Player, targetServer: String) {
            implementation?.requestTeleportNation(player, targetServer)
        }
    }
}