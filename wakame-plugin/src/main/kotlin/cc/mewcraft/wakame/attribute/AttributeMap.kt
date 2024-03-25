package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toBukkit
import com.google.common.collect.Multimap
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.UUID

sealed interface AttributeMap {
    fun getAttributeInstance(attribute: Attribute): AttributeInstance?

    fun getAttributeInstanceOrThrow(attribute: Attribute): AttributeInstance

    fun registerAttribute(attributeBase: Attribute)

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
    private val entity: Player,
) : AttributeMap {
    private val data: MutableMap<Attribute, AttributeInstance> = HashMap()

    override fun getAttributeInstance(attribute: Attribute): AttributeInstance? {
        // Kotlin 对于 Map.computeIfAbsent 中 lambda 的返回值做了非空要求
        // 因此这里手动实现了一遍 computeIfAbsent 以还原在 Java 下的行为
        // See: https://youtrack.jetbrains.com/issue/KT-10982
        val oldValue = data[attribute]
        if (oldValue == null) {
            if (attribute.vanilla) {
                val bukkitAttribute = attribute.toBukkit()
                val bukkitAttributeInstance = entity.getAttribute(bukkitAttribute)
                requireNotNull(bukkitAttributeInstance) { "Can't find vanilla attribute instance for attribute $attribute" }

                val attributeInstance = VanillaAttributeInstance(bukkitAttributeInstance)
                data[attribute] = attributeInstance
                return attributeInstance
            }

            val newValue = default.createAttributeInstance(attribute)
            if (newValue != null) {
                data[attribute] = newValue
                return newValue
            }
        }
        return oldValue
    }

    override fun getAttributeInstanceOrThrow(attribute: Attribute): AttributeInstance {
        return requireNotNull(getAttributeInstance(attribute)) { "Can't find attribute instance for attribute $attribute" }
    }

    override fun registerAttribute(attributeBase: Attribute) {
        if (attributeBase.vanilla) {
            val bukkitAttribute = attributeBase.toBukkit()
            entity.registerAttribute(bukkitAttribute)
            data[attributeBase] = VanillaAttributeInstance(entity.getAttribute(bukkitAttribute)!!)
            return
        }

        data[attributeBase] = WakameAttributeInstance(attributeBase)
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return data.containsKey(attribute)
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

    override fun removeAttributeModifiers(attributeModifiers: Multimap<Attribute, AttributeModifier>) {
        for ((attribute, modifiers) in attributeModifiers.asMap()) {
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
        data.values.forEach { instance -> instance.removeModifier(uuid) }
    }

    override fun clearAllModifiers() {
        data.values.forEach { instance -> instance.removeModifiers() }
    }

    override fun assignValues(other: AttributeMap) {
        require(other is PlayerAttributeMap) { "Can't assign values from non-PlayerAttributeMap" }
        other.data.values.forEach { instance -> getAttributeInstance(instance.attribute)?.replace(instance) }
    }
}

fun EntityAttributeMap(entity: Entity): EntityAttributeMap {
    return EntityAttributeMap(entity)
}

/**
 * This is a live object.
 *
 * The object does not actually store any attribute data about a non-player entity.
 * Instead, it works as an "accessor" to the underlying attribute data about an entity.
 * By design, the underlying attribute data is actually stored in the entity's NBT storage.
 */
class EntityAttributeMap(
    private val default: AttributeSupplier,
    entity: Entity,
) : AttributeMap {
    private val entity: WeakReference<Entity> = WeakReference(entity) // use WeakRef to prevent memory leak

    init {
        require(entity.type != EntityType.PLAYER) { "EntityAttributeMap can only be used for non-player entities" }
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

    override fun getAttributeInstanceOrThrow(attribute: Attribute): AttributeInstance {
        TODO("Not yet implemented")
    }

    override fun registerAttribute(attributeBase: Attribute) {
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