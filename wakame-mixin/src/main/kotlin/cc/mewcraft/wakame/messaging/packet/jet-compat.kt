package cc.mewcraft.wakame.messaging.packet

import cc.mewcraft.messaging2.handler.SimplePacket
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * 当玩家完成了 JEI 同步后, 发送此封包.
 *
 * @property playerId 完成 JEI 同步的玩家的唯一标识符
 */
class JEICompatSyncPacket : SimplePacket {
    lateinit var playerId: UUID

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(serverId: UUID, playerId: UUID) : super(serverId) {
        this.playerId = playerId
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
    }
}