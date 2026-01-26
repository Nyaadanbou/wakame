package cc.mewcraft.wakame.util.metadata

import cc.mewcraft.wakame.util.cooldown.Cooldown
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.Optional as JavaOptional

// ==================== Player Extensions ====================

/**
 * Gets the metadata map for this player.
 *
 * @return metadata map for this player
 */
fun Player.metadata(): MetadataMap = Metadata.provideForPlayer(this)

/**
 * Gets the optional metadata map for this player if it already exists.
 *
 * @return optional metadata map for this player
 */
fun Player.metadataOrNull(): JavaOptional<MetadataMap> = Metadata.getForPlayer(this)

/**
 * Gets a metadata value for this player by key.
 *
 * @param key the metadata key
 * @return optional containing the value
 */
inline fun <reified T : Any> Player.getMeta(key: MetadataKey<T>): JavaOptional<T> =
    metadata().get(key)

/**
 * Gets a metadata value for this player by key, or null if not present.
 *
 * @param key the metadata key
 * @return the value, or null if not present
 */
inline fun <reified T : Any> Player.getMetaOrNull(key: MetadataKey<T>): T? =
    metadata().getOrNull(key)

/**
 * Gets a metadata value for this player by key, or a default if not present.
 *
 * @param key the metadata key
 * @param default the default value
 * @return the value, or default if not present
 */
inline fun <reified T : Any> Player.getMetaOrDefault(key: MetadataKey<T>, default: T): T =
    metadata().getOrDefault(key, default)

/**
 * Sets a metadata value for this player by key.
 *
 * @param key the metadata key
 * @param value the value to set
 */
inline fun <reified T : Any> Player.setMeta(key: MetadataKey<T>, value: T) {
    metadata().put(key, value)
}

/**
 * Sets a metadata value for this player by key, only if not already set.
 *
 * @param key the metadata key
 * @param value the value to set
 * @return true if the value was set, false if already present
 */
inline fun <reified T : Any> Player.setMetaIfAbsent(key: MetadataKey<T>, value: T): Boolean =
    metadata().putIfAbsent(key, value)

/**
 * Removes a metadata value for this player by key.
 *
 * @param key the metadata key
 * @return true if a value was removed
 */
fun Player.removeMeta(key: MetadataKey<*>): Boolean =
    metadata().remove(key)

/**
 * Checks if this player has a metadata value for the given key.
 *
 * @param key the metadata key
 * @return true if the key exists
 */
fun Player.hasMeta(key: MetadataKey<*>): Boolean =
    metadata().has(key)

// ==================== Entity Extensions ====================

/**
 * Gets the metadata map for this entity.
 *
 * @return metadata map for this entity
 */
fun Entity.metadata(): MetadataMap = Metadata.provideForEntity(this)

/**
 * Gets the optional metadata map for this entity if it already exists.
 *
 * @return optional metadata map for this entity
 */
fun Entity.metadataOrNull(): JavaOptional<MetadataMap> = Metadata.getForEntity(this)

/**
 * Gets a metadata value for this entity by key.
 *
 * @param key the metadata key
 * @return optional containing the value
 */
inline fun <reified T : Any> Entity.getMeta(key: MetadataKey<T>): JavaOptional<T> =
    metadata().get(key)

/**
 * Gets a metadata value for this entity by key, or null if not present.
 *
 * @param key the metadata key
 * @return the value, or null if not present
 */
inline fun <reified T : Any> Entity.getMetaOrNull(key: MetadataKey<T>): T? =
    metadata().getOrNull(key)

/**
 * Gets a metadata value for this entity by key, or a default if not present.
 *
 * @param key the metadata key
 * @param default the default value
 * @return the value, or default if not present
 */
inline fun <reified T : Any> Entity.getMetaOrDefault(key: MetadataKey<T>, default: T): T =
    metadata().getOrDefault(key, default)

/**
 * Sets a metadata value for this entity by key.
 *
 * @param key the metadata key
 * @param value the value to set
 */
inline fun <reified T : Any> Entity.setMeta(key: MetadataKey<T>, value: T) {
    metadata().put(key, value)
}

/**
 * Sets a metadata value for this entity by key, only if not already set.
 *
 * @param key the metadata key
 * @param value the value to set
 * @return true if the value was set, false if already present
 */
inline fun <reified T : Any> Entity.setMetaIfAbsent(key: MetadataKey<T>, value: T): Boolean =
    metadata().putIfAbsent(key, value)

/**
 * Removes a metadata value for this entity by key.
 *
 * @param key the metadata key
 * @return true if a value was removed
 */
fun Entity.removeMeta(key: MetadataKey<*>): Boolean =
    metadata().remove(key)

/**
 * Checks if this entity has a metadata value for the given key.
 *
 * @param key the metadata key
 * @return true if the key exists
 */
fun Entity.hasMeta(key: MetadataKey<*>): Boolean =
    metadata().has(key)

// ==================== Block Extensions ====================

/**
 * Gets the metadata map for this block.
 *
 * @return metadata map for this block
 */
fun Block.metadata(): MetadataMap = Metadata.provideForBlock(this)

/**
 * Gets the optional metadata map for this block if it already exists.
 *
 * @return optional metadata map for this block
 */
fun Block.metadataOrNull(): JavaOptional<MetadataMap> = Metadata.getForBlock(this)

/**
 * Gets a metadata value for this block by key.
 *
 * @param key the metadata key
 * @return optional containing the value
 */
inline fun <reified T : Any> Block.getMeta(key: MetadataKey<T>): JavaOptional<T> =
    metadata().get(key)

/**
 * Gets a metadata value for this block by key, or null if not present.
 *
 * @param key the metadata key
 * @return the value, or null if not present
 */
inline fun <reified T : Any> Block.getMetaOrNull(key: MetadataKey<T>): T? =
    metadata().getOrNull(key)

/**
 * Gets a metadata value for this block by key, or a default if not present.
 *
 * @param key the metadata key
 * @param default the default value
 * @return the value, or default if not present
 */
inline fun <reified T : Any> Block.getMetaOrDefault(key: MetadataKey<T>, default: T): T =
    metadata().getOrDefault(key, default)

/**
 * Sets a metadata value for this block by key.
 *
 * @param key the metadata key
 * @param value the value to set
 */
inline fun <reified T : Any> Block.setMeta(key: MetadataKey<T>, value: T) {
    metadata().put(key, value)
}

/**
 * Sets a metadata value for this block by key, only if not already set.
 *
 * @param key the metadata key
 * @param value the value to set
 * @return true if the value was set, false if already present
 */
inline fun <reified T : Any> Block.setMetaIfAbsent(key: MetadataKey<T>, value: T): Boolean =
    metadata().putIfAbsent(key, value)

/**
 * Removes a metadata value for this block by key.
 *
 * @param key the metadata key
 * @return true if a value was removed
 */
fun Block.removeMeta(key: MetadataKey<*>): Boolean =
    metadata().remove(key)

/**
 * Checks if this block has a metadata value for the given key.
 *
 * @param key the metadata key
 * @return true if the key exists
 */
fun Block.hasMeta(key: MetadataKey<*>): Boolean =
    metadata().has(key)

// ==================== World Extensions ====================

/**
 * Gets the metadata map for this world.
 *
 * @return metadata map for this world
 */
fun World.metadata(): MetadataMap = Metadata.provideForWorld(this)

/**
 * Gets the optional metadata map for this world if it already exists.
 *
 * @return optional metadata map for this world
 */
fun World.metadataOrNull(): JavaOptional<MetadataMap> = Metadata.getForWorld(this)

/**
 * Gets a metadata value for this world by key.
 *
 * @param key the metadata key
 * @return optional containing the value
 */
inline fun <reified T : Any> World.getMeta(key: MetadataKey<T>): JavaOptional<T> =
    metadata().get(key)

/**
 * Gets a metadata value for this world by key, or null if not present.
 *
 * @param key the metadata key
 * @return the value, or null if not present
 */
inline fun <reified T : Any> World.getMetaOrNull(key: MetadataKey<T>): T? =
    metadata().getOrNull(key)

/**
 * Gets a metadata value for this world by key, or a default if not present.
 *
 * @param key the metadata key
 * @param default the default value
 * @return the value, or default if not present
 */
inline fun <reified T : Any> World.getMetaOrDefault(key: MetadataKey<T>, default: T): T =
    metadata().getOrDefault(key, default)

/**
 * Sets a metadata value for this world by key.
 *
 * @param key the metadata key
 * @param value the value to set
 */
inline fun <reified T : Any> World.setMeta(key: MetadataKey<T>, value: T) {
    metadata().put(key, value)
}

/**
 * Sets a metadata value for this world by key, only if not already set.
 *
 * @param key the metadata key
 * @param value the value to set
 * @return true if the value was set, false if already present
 */
inline fun <reified T : Any> World.setMetaIfAbsent(key: MetadataKey<T>, value: T): Boolean =
    metadata().putIfAbsent(key, value)

/**
 * Removes a metadata value for this world by key.
 *
 * @param key the metadata key
 * @return true if a value was removed
 */
fun World.removeMeta(key: MetadataKey<*>): Boolean =
    metadata().remove(key)

/**
 * Checks if this world has a metadata value for the given key.
 *
 * @param key the metadata key
 * @return true if the key exists
 */
fun World.hasMeta(key: MetadataKey<*>): Boolean =
    metadata().has(key)

// ==================== MetadataKey Creation Extensions ====================

/**
 * Creates a MetadataKey for the given type.
 *
 * @param id the key id
 * @return a new metadata key for this type
 */
inline fun <reified T : Any> metadataKey(id: String): MetadataKey<T> =
    MetadataKey.create(id, T::class.java)

/**
 * Creates a MetadataKey for the given type using the class name as the id.
 *
 * @return a new metadata key for this type with id from simple class name
 */
inline fun <reified T : Any> metadataKey(): MetadataKey<T> =
    metadataKey(T::class.simpleName ?: T::class.java.simpleName)

/**
 * Creates a MetadataKey for String type.
 *
 * @param id the key id
 * @return a new metadata key for String
 */
fun metadataStringKey(id: String): MetadataKey<String> =
    MetadataKey.createStringKey(id)

/**
 * Creates a MetadataKey for Boolean type.
 *
 * @param id the key id
 * @return a new metadata key for Boolean
 */
fun metadataBooleanKey(id: String): MetadataKey<Boolean> =
    MetadataKey.createBooleanKey(id)

/**
 * Creates a MetadataKey for Integer type.
 *
 * @param id the key id
 * @return a new metadata key for Integer
 */
fun metadataIntKey(id: String): MetadataKey<Int> =
    MetadataKey.createIntegerKey(id)

/**
 * Creates a MetadataKey for Long type.
 *
 * @param id the key id
 * @return a new metadata key for Long
 */
fun metadataLongKey(id: String): MetadataKey<Long> =
    MetadataKey.createLongKey(id)

/**
 * Creates a MetadataKey for Double type.
 *
 * @param id the key id
 * @return a new metadata key for Double
 */
fun metadataDoubleKey(id: String): MetadataKey<Double> =
    MetadataKey.createDoubleKey(id)

/**
 * Creates a MetadataKey for Float type.
 *
 * @param id the key id
 * @return a new metadata key for Float
 */
fun metadataFloatKey(id: String): MetadataKey<Float> =
    MetadataKey.createFloatKey(id)

/**
 * Creates a MetadataKey for Short type.
 *
 * @param id the key id
 * @return a new metadata key for Short
 */
fun metadataShortKey(id: String): MetadataKey<Short> =
    MetadataKey.createShortKey(id)

/**
 * Creates a MetadataKey for Char type.
 *
 * @param id the key id
 * @return a new metadata key for Char
 */
fun metadataCharKey(id: String): MetadataKey<Char> =
    MetadataKey.createCharacterKey(id)

/**
 * Creates a MetadataKey for Empty type (useful for flag-like metadata).
 *
 * @param id the key id
 * @return a new metadata key for Empty
 */
fun metadataEmptyKey(id: String): MetadataKey<Empty> =
    MetadataKey.createEmptyKey(id)

/**
 * Creates a MetadataKey for UUID type.
 *
 * @param id the key id
 * @return a new metadata key for UUID
 */
fun metadataUuidKey(id: String): MetadataKey<java.util.UUID> =
    MetadataKey.createUuidKey(id)

/**
 * Creates a MetadataKey for Cooldown type.
 *
 * @param id the key id
 * @return a new metadata key for Cooldown
 */
fun metadataCooldownKey(id: String): MetadataKey<Cooldown> =
    MetadataKey.createCooldownKey(id)

