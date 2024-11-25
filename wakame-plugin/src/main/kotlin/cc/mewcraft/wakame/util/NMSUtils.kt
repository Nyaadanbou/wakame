@file:Suppress("unused")

package cc.mewcraft.wakame.util

import net.minecraft.core.RegistryAccess
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Player

val World.serverLevel: ServerLevel
    get() = (this as CraftWorld).handle

val Chunk.levelChunk: LevelChunk
    get() = world.serverLevel.getChunk(x, z)

val Player.serverPlayer: ServerPlayer
    get() = (this as CraftPlayer).handle

val Player.connection: ServerGamePacketListenerImpl
    get() = serverPlayer.connection

val NamespacedKey.resourceLocation: ResourceLocation
    get() = ResourceLocation.fromNamespaceAndPath(namespace, key)

val ResourceLocation.namespacedKey: NamespacedKey
    get() = NamespacedKey(namespace, path)

internal val ResourceLocation.name: String
    get() = path

fun Player.send(vararg packets: Packet<*>) {
    val connection = connection
    packets.forEach { connection.send(it) }
}

fun Player.send(packets: Iterable<Packet<*>>) {
    val connection = connection
    packets.forEach { connection.send(it) }
}

fun Packet<*>.sendTo(vararg players: Player) {
    players.forEach { it.send(this) }
}

fun Packet<*>.sendTo(players: Iterable<Player>) {
    players.forEach { it.send(this) }
}

val MINECRAFT_SERVER: DedicatedServer = (Bukkit.getServer() as CraftServer).server
val REGISTRY_ACCESS: RegistryAccess = MINECRAFT_SERVER.registryAccess()
val DATA_VERSION: Int = CraftMagicNumbers.INSTANCE.dataVersion

val serverTick: Int
    get() = MINECRAFT_SERVER.tickCount
