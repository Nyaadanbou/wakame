package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import java.lang.ref.WeakReference
import java.util.UUID

sealed interface AttributeMap {
    /**
     * Registers an [attribute] to this map. This will overwrite any existing instance.
     */
    fun register(attribute: Attribute)

    /**
     * Gets an [AttributeInstance] specified by the [attribute].
     *
     * Returns `null` if the specified instance does not exist in this map.
     */
    fun getInstance(attribute: Attribute): AttributeInstance?

    /**
     * Checks whether this map has the [attribute].
     */
    fun hasAttribute(attribute: Attribute): Boolean
    fun hasModifier(attribute: Attribute, uuid: UUID): Boolean
    fun getValue(attribute: Attribute): Double
    fun getBaseValue(attribute: Attribute): Double
    fun getModifierValue(attribute: Attribute, uuid: UUID): Double

    /**
     * Assigns all the data from [other] to this map.
     */
    fun assignValues(other: AttributeMap)

    operator fun get(attribute: Attribute): AttributeInstance? {
        return getInstance(attribute)
    }
}

/**
 * Creates a new Player Attribute Map.
 */
fun PlayerAttributeMap(user: User<Player>): PlayerAttributeMap {
    return PlayerAttributeMap(DefaultAttributes.getSupplier(EntityType.PLAYER), user.player)
}

/**
 * This is a live object.
 *
 * The object contains attribute data about a player.
 *
 * By design, the object's lifecycle is the same as [Player]. That is, the object
 * is created when the player joins the server and destroyed after the player
 * quits the server.
 */
class PlayerAttributeMap
internal constructor(
    private val default: AttributeSupplier,
    private val player: Player,
) : AttributeMap {
    private val data: MutableMap<Attribute, AttributeInstance> = HashMap()

    init {
        // vanilla attribute instances must be "initialized"
        // otherwise, if we have changed the value of a vanilla attribute,
        // the value would not be actually applied to the world state.
        default.attributes.filter(Attribute::vanilla).forEach(::getInstance)
    }

    override fun getInstance(attribute: Attribute): AttributeInstance? {
        // the implementation has side effect to the player
        return data.getNullableOrPut(attribute) { default.createAttributeInstance(attribute, player) }
    }

    override fun register(attribute: Attribute) {
        data[attribute] = AttributeInstanceFactory.createInstance(attribute, player)
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return data[attribute] != null || default.hasAttribute(attribute)
    }

    override fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        return data[attribute]?.getModifier(uuid) != null || default.hasModifier(attribute, uuid)
    }

    override fun getValue(attribute: Attribute): Double {
        return data[attribute]?.getValue() ?: default.getValue(attribute)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return data[attribute]?.getBaseValue() ?: default.getBaseValue(attribute)
    }

    override fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        return data[attribute]?.getModifier(uuid)?.amount ?: default.getModifierValue(attribute, uuid)
    }

    override fun assignValues(other: AttributeMap) {
        require(other is PlayerAttributeMap) { "Can't assign values from for non-player AttributeMap" }
        other.data.values.forEach { getInstance(it.attribute)?.replace(it) }
    }

    /**
     * A modified version of [MutableMap.getOrPut] with the difference in that
     * the lambda [defaultValue] can return `null`. Furthermore, if the [defaultValue]
     * returns `null`, the [receiver map][this] will remain unchanged and `null` will
     * be returned for this function.
     */
    private inline fun <K, V> MutableMap<K, V>.getNullableOrPut(key: K, defaultValue: () -> V?): V? {
        val value = get(key)
        return if (value == null) {
            val answer = defaultValue()
            if (answer != null) {
                put(key, answer)
            }
            answer
        } else {
            value
        }
    }
}

fun EntityAttributeMap(entity: LivingEntity): EntityAttributeMap {
    return EntityAttributeMap(DefaultAttributes.getSupplier(entity.type), entity)
}

/**
 * This is a live object.
 *
 * The object does not actually store any attribute data about a non-player entity.
 * Instead, it works as an "accessor" to the underlying attribute data about an entity.
 * By design, the underlying attribute data is actually stored in the entity's NBT storage.
 */
class EntityAttributeMap
internal constructor(
    private val default: AttributeSupplier,
    entity: LivingEntity,
) : AttributeMap {
    private val entity: WeakReference<LivingEntity> =
        WeakReference(entity) // use WeakRef to prevent memory leak
    private val data: PersistentDataContainer
        get() = entity.get()?.persistentDataContainer ?: error("The entity reference object no longer exists")

    init {
        require(entity !is Player) { "EntityAttributeMap can only be used for non-player living entities" }
    }

    // TODO Some thoughts about implementation:
    //  The AttributeMap data should be stored in the entity's NBT storage,
    //  not in a property of `this`, since we want the data to be persistent
    //  on server restart.
    //  The root reason for this is because the items equipped on non-player
    //  entities are purely visual by design - they do not provide any effects. As such
    //  the attribute data are therefore provided by external sources, such as
    //  scripts and configs. That is, the attribute data are provided only ONCE,
    //  usually upon the entity is spawned. As a result, the attributes data
    //  must be persistent.

    override fun getInstance(attribute: Attribute): AttributeInstance? {
        TODO("Not yet implemented")
    }

    override fun register(attribute: Attribute) {
        TODO("Not yet implemented")
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override fun getValue(attribute: Attribute): Double {
        TODO("Not yet implemented")
    }

    override fun getBaseValue(attribute: Attribute): Double {
        TODO("Not yet implemented")
    }

    override fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        TODO("Not yet implemented")
    }

    override fun assignValues(other: AttributeMap) {
        TODO("Not yet implemented")
    }
}