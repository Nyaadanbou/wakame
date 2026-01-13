/**
 * TownyNetwork 协议概述.
 *
 * 目前协议有两个功能:
 * - 将玩家直接传送到指定服务器里的城镇传送点.
 * - 将玩家直接传送到指定服务器里的国家传送点.
 * - 遵循 Towny 关于传送的权限/经济/冷却规则. (实现有点麻烦)
 *
 * 请求跨服城镇传送时, 协议流程如下:
 * - 玩家请求传送到目标服务器的城镇传送点, 广播一次 [TownSpawnRequestPacket].
 * - 对于每个服务器, 收到 [TownSpawnRequestPacket] 后:
 *   - 如果 [TownSpawnRequestPacket.targetServer] 与当前服务器不一致, 则忽略; 否则继续.
 *   - 如果 [TownSpawnRequestPacket.playerId] 在当前服务器没有城镇, 则广播 [TownSpawnResponsePacket] 并附上拒绝原因.
 * - 对于每个服务器, 收到 [TownSpawnResponsePacket] 后:
 *   - 根据 [TownSpawnResponsePacket.response] 进行处理...
 *   - 如果是 [TownSpawnResponsePacket.ResponseType.ALLOW] 则让代理将玩家转移到目标服务器; 否则提示玩家并终止流程.
 *
 * 请求跨服国家传送时, 协议流程基本同城镇的.
 */
package cc.mewcraft.wakame.messaging.handler

import cc.mewcraft.wakame.integration.townynetwork.TownylessNetworkImpl
import cc.mewcraft.wakame.messaging.packet.NationSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.NationSpawnResponsePacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnResponsePacket

/**
 * 该 [KoishPacketHandler] 负责处理以下封包:
 *
 * - [TownSpawnRequestPacket]
 * - [TownSpawnResponsePacket]
 * - [NationSpawnRequestPacket]
 * - [NationSpawnResponsePacket]
 */
interface TownyNetworkPacketHandler : KoishPacketHandler {
    fun handle(packet: TownSpawnRequestPacket)
    fun handle(packet: TownSpawnResponsePacket)
    fun handle(packet: NationSpawnRequestPacket)
    fun handle(packet: NationSpawnResponsePacket)

    companion object : TownyNetworkPacketHandler {
        private var implementation: TownyNetworkPacketHandler = TownylessNetworkImpl

        fun setImplementation(impl: TownyNetworkPacketHandler) {
            this.implementation = impl
        }

        override fun handle(packet: TownSpawnRequestPacket) {
            implementation.handle(packet)
        }

        override fun handle(packet: TownSpawnResponsePacket) {
            implementation.handle(packet)
        }

        override fun handle(packet: NationSpawnRequestPacket) {
            implementation.handle(packet)
        }

        override fun handle(packet: NationSpawnResponsePacket) {
            implementation.handle(packet)
        }
    }
}
