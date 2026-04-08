package cc.mewcraft.wakame.messaging.packet

import cc.mewcraft.messaging2.handler.SimplePacket
import io.netty.buffer.ByteBuf
import java.util.*


/**
 * 当玩家请求一个跨服城镇传送点时发送此封包.
 *
 * @property playerId 请求传送的玩家的唯一标识符
 * @property targetServer 目标服务器的名字 (跟代理中的名字一致)
 */
class TownSpawnRequestPacket : SimplePacket {

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
class TownSpawnResponsePacket : SimplePacket {

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
 * 当玩家请求一个跨服城镇前哨战时发送此封包.
 *
 * @property playerId 请求传送点的玩家的唯一标识符
 * @property targetServer 目标服务器的名字 (跟代理中的名字一致)
 * @property index 目标前哨战编号, 采用 1-based 索引
 */
class TownOutpostRequestPacket : SimplePacket {

    lateinit var playerId: UUID
    lateinit var targetServer: String
    var index: Int = 1

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID, targetServer: String, index: Int) : super(serverId) {
        this.playerId = playerId
        this.targetServer = targetServer
        this.index = index
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.targetServer = this.readString(buffer)
        this.index = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeString(this.targetServer, buffer)
        buffer.writeInt(this.index)
    }

    override fun toString(): String {
        return "TownOutpostRequestPacket(playerId=$playerId, targetServer=$targetServer, index=$index)"
    }
}

/**
 * 服务器响应玩家的跨服城镇前哨战请求时发送此封包.
 *
 * @property playerId 请求传送点的玩家的唯一标识符
 */
class TownOutpostResponsePacket : SimplePacket {

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
        return "TownOutpostResponsePacket(playerId=$playerId, response=$response)"
    }

    enum class ResponseType {
        ALLOW,
        DENY_FOR_NO_TOWN,
        DENY_FOR_NO_SUCH_OUTPOST,
    }
}

/**
 * 当玩家请求一个跨服国家传送点时发送此封包.
 *
 * @property playerId 请求传送点的玩家的唯一标识符
 * @property targetServer 目标服务器的名字 (跟代理中的名字一致)
 */
class NationSpawnRequestPacket : SimplePacket {

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
class NationSpawnResponsePacket : SimplePacket {

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
