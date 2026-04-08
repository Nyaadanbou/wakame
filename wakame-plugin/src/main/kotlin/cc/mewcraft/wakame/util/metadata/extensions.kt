package cc.mewcraft.wakame.util.metadata

import cc.mewcraft.wakame.util.cooldown.Cooldown
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.Optional as JavaOptional

// ==================== Bukkit Objects Extensions ====================

/**
 * Gets the metadata map for this player, creating one if it doesn't exist.
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
 * Gets the metadata map for this entity, creating one if it doesn't exist.
 *
 * @return metadata map for this entity
 */
fun Entity.metadata(): MetadataMap = if (this is Player) {
    Metadata.provideForPlayer(this)
} else {
    Metadata.provideForEntity(this)
}

/**
 * Gets the optional metadata map for this entity if it already exists.
 *
 * @return optional metadata map for this entity
 */
fun Entity.metadataOrNull(): JavaOptional<MetadataMap> = if (this is Player) {
    Metadata.getForPlayer(this)
} else {
    Metadata.getForEntity(this)
}

/**
 * Gets the metadata map for this block, creating one if it doesn't exist.
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
 * Gets the metadata map for this world, creating one if it doesn't exist.
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

