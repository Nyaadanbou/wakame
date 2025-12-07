/**
 * Towny 相关封包.
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
package cc.mewcraft.wakame.messaging.packet

import cc.mewcraft.wakame.messaging.KoishPacket
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * 该接口封装了当服务器接收到封包时如何处理的具体逻辑.
 */
interface TownyNetworkHandler {
    fun handle(packet: TownSpawnRequestPacket)
    fun handle(packet: TownSpawnResponsePacket)
    fun handle(packet: NationSpawnRequestPacket)
    fun handle(packet: NationSpawnResponsePacket)

    companion object : TownyNetworkHandler {
        private var implementation: TownyNetworkHandler? = null

        fun setImplementation(impl: TownyNetworkHandler) {
            this.implementation = impl
        }

        override fun handle(packet: TownSpawnRequestPacket) {
            implementation?.handle(packet)
        }

        override fun handle(packet: TownSpawnResponsePacket) {
            implementation?.handle(packet)
        }

        override fun handle(packet: NationSpawnRequestPacket) {
            implementation?.handle(packet)
        }

        override fun handle(packet: NationSpawnResponsePacket) {
            implementation?.handle(packet)
        }
    }
}

/**
 * 当玩家请求一个跨服城镇传送点时发送此封包.
 *
 * @property playerId 请求传送的玩家的唯一标识符
 * @property targetServer 目标服务器的名字 (跟代理中的名字一致)
 */
class TownSpawnRequestPacket : KoishPacket {

    lateinit var playerId: UUID
    lateinit var targetServer: String

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID, targetServer: String) : super(serverId) {
        this.playerId = playerId
        this.targetServer = targetServer
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.targetServer = this.readString(buffer)
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeString(this.targetServer, buffer)
    }

    override fun toString(): String {
        return "TownSpawnRequestPacket(playerId=$playerId, targetServer=$targetServer)"
    }
}

/**
 * 服务器响应玩家的跨服城镇传送点请求时发送此封包.
 *
 * @property playerId 请求传送点的玩家的唯一标识符
 */
class TownSpawnResponsePacket : KoishPacket {

    lateinit var playerId: UUID
    lateinit var response: ResponseType

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID, response: ResponseType) : super(serverId) {
        this.playerId = playerId
        this.response = response
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.response = this.readEnum<ResponseType>(buffer)
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeEnum(this.response, buffer)
    }

    override fun toString(): String {
        return "TownSpawnResponsePacket(playerId=$playerId, response=$response)"
    }

    enum class ResponseType {
        ALLOW,
        DENY_FOR_NO_TOWN,
    }
}

/**
 * 当玩家请求一个跨服国家传送点时发送此封包.
 *
 * @property playerId 请求传送点的玩家的唯一标识符
 * @property targetServer 目标服务器的名字 (跟代理中的名字一致)
 */
class NationSpawnRequestPacket : KoishPacket {

    lateinit var playerId: UUID
    lateinit var targetServer: String

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID, targetServer: String) : super(serverId) {
        this.playerId = playerId
        this.targetServer = targetServer
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.targetServer = this.readString(buffer)
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeString(this.targetServer, buffer)
    }

    override fun toString(): String {
        return "NationSpawnRequestPacket(playerId=$playerId, targetServer=$targetServer)"
    }
}

/**
 * 服务器响应玩家的跨服国家传送点请求时发送此封包.
 *
 * @property playerId 请求传送点的玩家的唯一标识符
 */
class NationSpawnResponsePacket : KoishPacket {

    lateinit var playerId: UUID
    lateinit var response: ResponseType

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID, response: ResponseType) : super(serverId) {
        this.playerId = playerId
        this.response = response
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.response = this.readEnum<ResponseType>(buffer)
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeEnum(this.response, buffer)
    }

    override fun toString(): String {
        return "NationSpawnResponsePacket(playerId=$playerId, response=$response)"
    }

    enum class ResponseType {
        ALLOW,
        DENY_FOR_NO_NATION,
    }
}
