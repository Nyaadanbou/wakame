package cc.mewcraft.extracontexts.common.messaging.packet

import cc.mewcraft.messaging2.handler.SimplePacket
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * Cache invalidation packet for KeyValueStore data modifications.
 *
 * @property playerId UUID of the player affected by the cache invalidation.
 * @property type Type of cache invalidation (single, prefix, or all).
 * @property data List of keys affected by the invalidation.
 */
class CacheInvalidationPacket : SimplePacket {

    /**
     * UUID of the player affected by the cache invalidation.
     */
    lateinit var playerId: UUID

    /**
     * Type of cache invalidation.
     */
    lateinit var type: InvalidationType

    /**
     * - 当 [type] = [InvalidationType.ALL] 时, 此列表为空.
     * - 当 [type] = [InvalidationType.PREFIX] 时, 此列表包含 1 个元素, 为所删除的前缀.
     * - 当 [type] = [InvalidationType.SINGLE] 时, 此列表包含所有被更新或删除的具体键名.
     */
    lateinit var data: List<String>

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
        this.data = keys
    }

    override fun read(buffer: ByteBuf) {
        this.playerId = this.readUUID(buffer)
        this.type = this.readEnum<InvalidationType>(buffer)
        val size = buffer.readInt()
        this.data = (0 until size).map { this.readString(buffer) }
    }

    override fun write(buffer: ByteBuf) {
        this.writeUUID(this.playerId, buffer)
        this.writeEnum(this.type, buffer)
        buffer.writeInt(this.data.size)
        this.data.forEach { this.writeString(it, buffer) }
    }

    override fun toString(): String {
        return "CacheInvalidationPacket(playerId=$playerId, type=$type, keys=$data)"
    }

    enum class InvalidationType {
        /**
         * Single key value was updated.
         */
        SINGLE,

        /**
         * Keys with a specific prefix were updated.
         */
        PREFIX,

        /**
         * All player data was cleared.
         */
        ALL,
    }
}