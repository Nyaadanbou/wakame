package cc.mewcraft.wakame.messaging

import io.netty.buffer.ByteBuf
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import ninja.egg82.messenger.packets.AbstractPacket
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Function

abstract class KoishPacket(sender: UUID) : AbstractPacket(sender) {

    private val componentSerializer = GsonComponentSerializer.gson()

    protected fun writeComponent(component: Component, buffer: ByteBuf) {
        this.writeString(this.componentSerializer.serialize(component), buffer)
    }

    protected fun readComponent(buffer: ByteBuf): Component {
        return this.componentSerializer.deserialize(this.readString(buffer))
    }

    protected fun writeKey(key: Key, buffer: ByteBuf) {
        this.writeString(key.asString(), buffer)
    }

    protected fun readKey(buffer: ByteBuf): Key {
        val value = this.readString(buffer)
        val key = Key.key(value)
        return key
    }

    protected fun <K, V> writeMap(
        map: MutableMap<K, V>,
        keyWriter: BiConsumer<K, ByteBuf>,
        valueWriter: BiConsumer<V, ByteBuf>,
        buffer: ByteBuf,
    ) {
        this.writeVarInt(map.size, buffer)

        for (entry in map.entries) {
            keyWriter.accept(entry.key, buffer)
            valueWriter.accept(entry.value, buffer)
        }
    }

    protected fun <K, V> readMap(
        buffer: ByteBuf,
        keyReader: Function<ByteBuf, K>,
        valueReader: Function<ByteBuf, V>,
    ): MutableMap<K, V> {
        val size = this.readVarInt(buffer)
        val map = HashMap<K, V>()

        for (i in 0..<size) {
            map[keyReader.apply(buffer)] = valueReader.apply(buffer)
        }

        return map
    }

    protected fun <E : Enum<E>> writeEnum(value: E, buf: ByteBuf) {
        this.writeVarInt(value.ordinal, buf)
    }

    protected fun <E : Enum<E>> readEnum(buf: ByteBuf, cls: Class<E>): E {
        return cls.enumConstants[this.readVarInt(buf)]
    }

    protected inline fun <reified E : Enum<E>> readEnum(buf: ByteBuf): E {
        return readEnum(buf, E::class.java)
    }
}