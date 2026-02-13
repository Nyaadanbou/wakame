@file:Suppress("unused")

package cc.mewcraft.wakame.util

import com.google.common.collect.MapMaker
import io.netty.buffer.Unpooled
import net.kyori.adventure.key.Key
import net.minecraft.core.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.Identifier
import net.minecraft.resources.RegistryOps.RegistryInfoLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.damage.CraftDamageSource
import org.bukkit.craftbukkit.enchantments.CraftEnchantment
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.damage.DamageSource
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentMap
import kotlin.jvm.optionals.getOrNull

val MINECRAFT_SERVER: DedicatedServer by lazy { (Bukkit.getServer() as CraftServer).server }
val REGISTRY_ACCESS: RegistryAccess by lazy { MINECRAFT_SERVER.registryAccess() }
val RECIPE_MANAGER: RecipeManager by lazy { MINECRAFT_SERVER.recipeManager }
val DATA_VERSION: Int by lazy { CraftMagicNumbers.INSTANCE.dataVersion }

val MojangUnit.toKotlin: Unit
    get() = Unit

val Unit.toMojang: MojangUnit
    get() = MojangUnit.INSTANCE

val World.serverLevel: ServerLevel
    get() = (this as CraftWorld).handle

val Chunk.levelChunk: LevelChunk
    get() = world.serverLevel.getChunk(x, z)

val Player.serverPlayer: ServerPlayer
    get() = (this as CraftPlayer).handle

val Entity.handle: MojangEntity
    get() = (this as CraftEntity).handle

val DamageSource.handle: MojangDamageSource
    get() = (this as CraftDamageSource).handle

val serverTick: Int
    get() = MINECRAFT_SERVER.tickCount

val Enchantment.handle: MojangEnchantment
    get() = (this as CraftEnchantment).handle

fun Key.toIdentifier(): Identifier =
    Identifier.fromNamespaceAndPath(namespace(), value())

operator fun <T : Any> Registry<T>.get(key: String): Optional<Holder.Reference<T>> {
    val id = Identifier.tryParse(key) ?: return Optional.empty()
    return get(id)
}

fun <T : Any> Registry<T>.get(id: Identifier): Holder<T>? {
    val key = ResourceKey.create(key(), id)
    return get(key).getOrNull()
}

fun <T : Any> Registry<T>.getOrNull(key: String): Holder.Reference<T>? {
    return get(key).getOrNull()
}

fun <T : Any> Registry<T>.getOrNull(id: Identifier): Holder.Reference<T>? {
    return get(id).getOrNull()
}

fun <T : Any> Registry<T>.getOrNull(key: Key): Holder.Reference<T>? {
    return getOrNull(Identifier.fromNamespaceAndPath(key.namespace(), key.value()))
}

fun <T : Any> Registry<T>.getOrThrow(key: String): Holder<T> {
    return getOrThrow(Identifier.parse(key))
}

fun <T : Any> Registry<T>.getOrThrow(id: Identifier): Holder<T> {
    val key = ResourceKey.create(key(), id)
    return getOrThrow(key)
}

fun <T : Any> Registry<T>.getOrThrow(key: Key): Holder<T> {
    return getOrThrow(Identifier.fromNamespaceAndPath(key.namespace(), key.value()))
}

fun <T : Any> Registry<T>.getValue(key: String?): T? {
    return getValue(key?.let(Identifier::parse))
}

fun <T : Any> Registry<T>.getValue(key: Key?): T? {
    return getValue(key?.toIdentifier())
}

fun <T : Any> DefaultedRegistry<T>.getValue(key: String?): T {
    return getValue(key?.let(Identifier::parse))
}

fun <T : Any> DefaultedRegistry<T>.getValue(key: Key?): T {
    return getValue(key?.toIdentifier())
}

fun <T : Any> Registry<T>.getValueOrThrow(key: String): T {
    return getValueOrThrow(Identifier.parse(key))
}

fun <T : Any> Registry<T>.getValueOrThrow(id: Identifier): T {
    return getOrThrow(ResourceKey.create(key(), id)).value()
}

fun <T : Any> Registry<T>.getOrCreateHolder(id: Identifier): Holder<T> {
    val key = ResourceKey.create(key(), id)
    val holder = get(key)

    if (holder.isPresent)
        return holder.get()

    if (this !is MappedRegistry<T>)
        throw IllegalStateException("Can't create holder for non MappedRegistry ${this.key()}")

    return this.createRegistrationLookup().getOrThrow(key)
}

operator fun Registry<*>.contains(key: String): Boolean {
    val id = Identifier.tryParse(key) ?: return false
    return containsKey(id)
}

operator fun Registry<*>.contains(key: Key): Boolean {
    return containsKey(key.toIdentifier())
}

operator fun <T : Any> WritableRegistry<T>.set(name: String, value: T) {
    register(ResourceKey.create(key(), Identifier.parse(name)), value, RegistrationInfo.BUILT_IN)
}

operator fun <T : Any> WritableRegistry<T>.set(id: Identifier, value: T) {
    register(ResourceKey.create(key(), id), value, RegistrationInfo.BUILT_IN)
}

operator fun <T : Any> WritableRegistry<T>.set(key: ResourceKey<T>, value: T) {
    register(key, value, RegistrationInfo.BUILT_IN)
}

operator fun <T : Any> WritableRegistry<T>.set(key: Key, value: T) {
    register(key.toIdentifier(), value)
}

fun <T : Any> WritableRegistry<T>.register(id: Identifier, value: T): Holder.Reference<T> {
    return register(ResourceKey.create(key(), id), value, RegistrationInfo.BUILT_IN)
}

fun <T : Any> WritableRegistry<T>.register(id: Key, value: T): Holder.Reference<T> {
    return register(id.toIdentifier(), value)
}

fun <T : Any> Registry<T>.toHolderMap(): Map<Identifier, Holder<T>> {
    val map = HashMap<Identifier, Holder<T>>()
    for (key in registryKeySet()) {
        val holderOptional = get(key)
        if (holderOptional.isEmpty)
            continue

        map[key.identifier()] = holderOptional.get()
    }

    return map
}

fun <T : Any> Registry<T>.toMap(): Map<Identifier, T> {
    val map = HashMap<Identifier, T>()
    for (key in registryKeySet()) {
        val holderOptional = get(key)
        if (holderOptional.isEmpty)
            continue

        val holder = holderOptional.get()
        if (!holder.isBound)
            continue

        map[key.identifier()] = holder.value()
    }

    return map
}

operator fun <T : Any> ResourceKey<Registry<T>>.get(key: ResourceKey<T>): Holder.Reference<T>? {
    return REGISTRY_ACCESS.get(key).getOrNull()
}

operator fun <T : Any> ResourceKey<Registry<T>>.get(id: Identifier): Holder.Reference<T>? {
    return get(ResourceKey.create<T>(this, id))
}

operator fun <T : Any> ResourceKey<Registry<T>>.get(id: String): Holder.Reference<T>? {
    return get(Identifier.parse(id))
}

fun <T : Any> ResourceKey<Registry<T>>.getOrThrow(key: ResourceKey<T>): Holder.Reference<T> {
    return REGISTRY_ACCESS.get(key).get()
}

fun <T : Any> ResourceKey<Registry<T>>.getOrThrow(id: Identifier): Holder.Reference<T> {
    return REGISTRY_ACCESS.get(ResourceKey.create<T>(this, id)).get()
}

fun <T : Any> ResourceKey<Registry<T>>.getOrThrow(id: Key): Holder.Reference<T> {
    return getOrThrow(id.toIdentifier())
}

fun <T : Any> ResourceKey<Registry<T>>.getOrThrow(key: String): Holder.Reference<T> {
    return getOrThrow(Identifier.parse(key))
}

fun <T : Any> ResourceKey<Registry<T>>.getValue(id: Identifier): T? {
    return get(id)?.value()
}

fun <T : Any> ResourceKey<Registry<T>>.getValue(key: String): T? {
    return get(key)?.value()
}

fun <T : Any> RegistryAccess.getOrThrow(key: ResourceKey<T>): Holder.Reference<T> {
    return REGISTRY_ACCESS.get(key).get()
}

fun <T : Any> RegistryAccess.getValue(key: ResourceKey<T>): T? {
    return REGISTRY_ACCESS.get(key).getOrNull()?.value()
}

fun <T : Any> RegistryAccess.getValueOrThrow(key: ResourceKey<T>): T {
    return REGISTRY_ACCESS.get(key).get().value()
}

fun <T : Any> RegistryInfoLookup.lookupGetterOrThrow(key: ResourceKey<Registry<T>>): HolderGetter<T> {
    return lookup(key).getOrNull()?.getter ?: throw IllegalArgumentException("Registry not found: $key")
}

fun Identifier.toString(separator: String): String {
    return namespace + separator + path
}

val NamespacedKey.resourceLocation: Identifier
    get() = Identifier.fromNamespaceAndPath(namespace, key)

val Identifier.namespacedKey: NamespacedKey
    get() = NamespacedKey(namespace, path)

internal val Identifier.name: String
    get() = path

fun Player.send(vararg packets: Packet<*>) {
    val connection = serverPlayer.connection
    packets.forEach { connection.send(it) }
}

fun Player.send(packets: Iterable<Packet<*>>) {
    val connection = serverPlayer.connection
    packets.forEach { connection.send(it) }
}

fun Packet<*>.sendTo(vararg players: Player) {
    players.forEach { it.send(this) }
}

fun Packet<*>.sendTo(players: Iterable<Player>) {
    players.forEach { it.send(this) }
}

fun RegistryFriendlyByteBuf(): RegistryFriendlyByteBuf =
    RegistryFriendlyByteBuf(Unpooled.buffer(), REGISTRY_ACCESS)

fun <E : Any> NonNullList(list: List<E>, default: E? = null): NonNullList<E> {
    val nonNullList: NonNullList<E>
    if (default == null) {
        nonNullList = NonNullList.createWithCapacity(list.size)
        nonNullList.addAll(list)
    } else {
        nonNullList = NonNullList.withSize(list.size, default)
        list.forEachIndexed { index, e -> nonNullList[index] = e }
    }

    return nonNullList
}

object NMSUtils {
    private val ENTITY_ID_CACHE: ConcurrentMap<Int, Entity> = MapMaker().weakValues().makeMap()

    fun getEntity(entityId: Int, world: World? = null): Entity? {
        return ENTITY_ID_CACHE.computeIfAbsent(entityId) { id -> getEntityByIdWithWorld(id, world) }
    }

    private fun getEntityByIdWithWorld(entityId: Int, world: World?): Entity? {
        if (world != null) {
            return world.serverLevel.`moonrise$getEntityLookup`().get(entityId)?.bukkitEntity
        }
        for (world in Bukkit.getServer().worlds) {
            val entity = world.serverLevel.`moonrise$getEntityLookup`().get(entityId)?.bukkitEntity
            if (entity != null) {
                return entity
            }
        }
        return null
    }
}
