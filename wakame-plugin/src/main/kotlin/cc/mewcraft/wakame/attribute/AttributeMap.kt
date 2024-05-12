package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.User
import com.google.common.collect.Multimap
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import java.lang.ref.WeakReference
import java.util.UUID

sealed interface AttributeMap {
    fun getAttributeInstance(attribute: Attribute): AttributeInstance?
    fun registerAttribute(attribute: Attribute)
    fun hasAttribute(attribute: Attribute): Boolean
    fun hasModifier(attribute: Attribute, uuid: UUID): Boolean
    fun getValue(attribute: Attribute): Double
    fun getBaseValue(attribute: Attribute): Double
    fun getModifierValue(attribute: Attribute, uuid: UUID): Double
    fun addAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>)
    fun removeAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>)
    fun clearModifiers(uuid: UUID)
    fun clearAllModifiers()
    fun assignValues(other: AttributeMap)

    operator fun get(attribute: Attribute): AttributeInstance? {
        return getAttributeInstance(attribute)
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
class PlayerAttributeMap internal constructor(
    private val default: AttributeSupplier,
    private val player: Player,
) : AttributeMap {
    private val data: MutableMap<Attribute, AttributeInstance> = HashMap()

    init {
        // vanilla attribute instances must be "initialized"
        default.attributeTypes.filter(Attribute::vanilla).forEach(::getAttributeInstance)
    }

    override fun getAttributeInstance(attribute: Attribute): AttributeInstance? {
        return data.getNullableOrPut(attribute) {
            default.createAttributeInstance(attribute, player)
        }
    }

    override fun registerAttribute(attribute: Attribute) {
        if (attribute.vanilla) {
            data[attribute] = AttributeInstanceFactory.createInstance(attribute, player)
        } else {
            data[attribute] = AttributeInstanceFactory.createInstance(attribute, player)
        }
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        if (attribute.vanilla) {
            return getAttributeInstance(attribute) != null
        }
        return data.containsKey(attribute)
    }

    override fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        if (attribute.vanilla) {
            return getAttributeInstance(attribute)?.getModifier(uuid) != null
        }
        return data[attribute]?.getModifier(uuid) != null || default.hasModifier(attribute, uuid)
    }

    override fun getValue(attribute: Attribute): Double {
        if (attribute.vanilla) {
            getAttributeInstance(attribute) // ensure the attribute is registered
        }
        return data[attribute]?.getValue() ?: default.getValue(attribute)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        if (attribute.vanilla) {
            getAttributeInstance(attribute)
        }
        return data[attribute]?.getBaseValue() ?: default.getBaseValue(attribute)
    }

    override fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        if (attribute.vanilla) {
            getAttributeInstance(attribute)
        }
        return data[attribute]?.getModifier(uuid)?.amount ?: default.getModifierValue(attribute, uuid)
    }

    override fun removeAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>) {
        for ((attribute, modifiers) in attributeModifiers.asMap()) {
            if (attribute.vanilla) {
                getAttributeInstance(attribute)
            }
            data[attribute]?.let { modifiers.forEach(it::removeModifier) }
        }
    }

    override fun addAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>) {
        attributeModifiers.forEach { attribute, modifier ->
            getAttributeInstance(attribute)?.let { attributeInstance ->
                attributeInstance.removeModifier(modifier)
                attributeInstance.addModifier(modifier)
            }
        }
    }

    override fun clearModifiers(uuid: UUID) {
        data.values.forEach { it.removeModifier(uuid) }
    }

    override fun clearAllModifiers() {
        data.values.forEach { it.removeModifiers() }
    }

    override fun assignValues(other: AttributeMap) {
        require(other is PlayerAttributeMap) { "Can't assign values from for non-player AttributeMap" }
        other.data.values.forEach { getAttributeInstance(it.attribute)?.replace(it) }
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
class EntityAttributeMap internal constructor(
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

    override fun getAttributeInstance(attribute: Attribute): AttributeInstance? {
        TODO("Not yet implemented")
    }

    override fun registerAttribute(attribute: Attribute) {
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

    override fun addAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>) {
        TODO("Not yet implemented")
    }

    override fun removeAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>) {
        TODO("Not yet implemented")
    }

    override fun clearModifiers(uuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun clearAllModifiers() {
        TODO("Not yet implemented")
    }

    override fun assignValues(other: AttributeMap) {
        TODO("Not yet implemented")
    }
}