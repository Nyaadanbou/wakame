package cc.mewcraft.wakame.messaging.packet

import cc.mewcraft.messaging2.handler.SimplePacket
import io.netty.buffer.ByteBuf
import java.util.*


/**
 * 玩家明确请求传送到服务器时发送的封包。
 */
class TeleportOnJoinRequestPacket : SimplePacket {

    lateinit var playerId: UUID

    /**
     * [cc.mewcraft.messaging2.ServerInfoProvider.serverGroup]
     */
    lateinit var group: String

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID, group: String) : super(serverId) {
        this.playerId = playerId
        this.group = group
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.group = this.readString(buffer)
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeString(this.group, buffer)
    }

    override fun toString(): String {
        return "TeleportOnJoinRequestPacket(playerId=$playerId, group=$group)"
    }
}