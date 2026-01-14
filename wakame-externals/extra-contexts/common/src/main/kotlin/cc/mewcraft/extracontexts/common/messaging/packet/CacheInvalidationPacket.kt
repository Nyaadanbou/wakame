package cc.mewcraft.extracontexts.common.messaging.packet

import cc.mewcraft.messaging2.handler.SimplePacket
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * Cache invalidation packet for KeyValueStore data modifications.
 *
 * @property playerId Player UUID affected by the cache invalidation.
 * @property type Type of cache invalidation (single key, prefix, or all).
 * @property keys List of keys affected by the invalidation.
 */
class CacheInvalidationPacket : SimplePacket {

    lateinit var playerId: UUID
    lateinit var type: InvalidationType
    lateinit var keys: List<String>

    constructor(sender: UUID, buf: ByteBuf) : super(sender) {
        this.read(buf)
    }

    constructor(
        serverId: UUID,
        playerId: UUID,
        type: InvalidationType,
        keys: List<String>,
    ) : super(serverId) {
        this.playerId = playerId
        this.type = type
        this.keys = keys
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.type = this.readEnum<InvalidationType>(buffer)
        val size = buffer.readInt()
        this.keys = (0 until size).map { this.readString(buffer) }
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeEnum(this.type, buffer)
        buffer.writeInt(this.keys.size)
        this.keys.forEach { this.writeString(it, buffer) }
    }

    override fun toString(): String {
        return "CacheInvalidationPacket(playerId=$playerId, type=$type, keys=$keys)"
    }

    enum class InvalidationType {
        /**
         * Single key value was updated or deleted.
         */
        SINGLE_KEY,

        /**
         * Keys with a specific prefix were deleted.
         */
        PREFIX,

        /**
         * All player data was cleared.
         */
        ALL,
    }
}