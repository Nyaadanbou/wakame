package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.util.toBukkit
import com.google.common.collect.Multimap
import org.bukkit.entity.Player
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

    fun addAttributeModifiers(attributeModifiers: Multimap<out Attribute, AttributeModifier>)

    fun removeAttributeModifiers(attributeModifiers: Multimap<out Attribute, AttributeModifier>)

    fun clearModifiers(uuid: UUID)

    fun clearAllModifiers()

    fun assignValues(other: AttributeMap)

    operator fun get(attribute: Attribute): AttributeInstance? {
        return getAttributeInstance(attribute)
    }
}

/**
 * This class contains attribute information about a player.
 *
 * States of this object should be periodically updated by
 * [AttributeAccessor].
 *
 * Not thread-safe.
 */
class PlayerAttributeMap(
    private val defaultSupplier: AttributeSupplier,
    private val entity: Player,
) : AttributeMap {
    private val attributes: MutableMap<Attribute, AttributeInstance> = HashMap()

    override fun getAttributeInstance(attribute: Attribute): AttributeInstance? {
        // Kotlin 对于 Map.computeIfAbsent 中 lambda 的返回值做了非空要求
        // 因此这里手动实现了一遍 computeIfAbsent 以还原在 Java 下的行为
        // See: https://youtrack.jetbrains.com/issue/KT-10982
        val oldValue = attributes[attribute]
        if (oldValue == null) {
            if (attribute.vanilla) {
                val bukkitAttribute = attribute.toBukkit()
                val bukkitAttributeInstance = entity.getAttribute(bukkitAttribute)
                requireNotNull(bukkitAttributeInstance) { "Can't find vanilla attribute instance for attribute $attribute" }

                val attributeInstance = VanillaAttributeInstance(bukkitAttributeInstance)
                attributes[attribute] = attributeInstance
                return attributeInstance
            }

            val newValue = defaultSupplier.createAttributeInstance(attribute)
            if (newValue != null) {
                attributes[attribute] = newValue
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
            attributes[attributeBase] = VanillaAttributeInstance(entity.getAttribute(bukkitAttribute)!!)
            return
        }

        val attributeModifiable = WakameAttributeInstance(attributeBase)
        attributes[attributeBase] = attributeModifiable
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return attributes.containsKey(attribute)
    }

    override fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        return attributes[attribute]?.getModifier(uuid) != null || defaultSupplier.hasModifier(attribute, uuid)
    }

    override fun getValue(attribute: Attribute): Double {
        return attributes[attribute]?.getValue() ?: defaultSupplier.getValue(attribute)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return attributes[attribute]?.getBaseValue() ?: defaultSupplier.getBaseValue(attribute)
    }

    override fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        return attributes[attribute]?.getModifier(uuid)?.amount ?: defaultSupplier.getModifierValue(attribute, uuid)
    }

    override fun removeAttributeModifiers(attributeModifiers: Multimap<out Attribute, AttributeModifier>) {
        for ((attribute, modifiers) in attributeModifiers.asMap()) {
            attributes[attribute]?.let {
                modifiers.forEach(it::removeModifier)
            }
        }
    }

    override fun addAttributeModifiers(attributeModifiers: Multimap<out Attribute, AttributeModifier>) {
        attributeModifiers.forEach { attribute, modifier ->
            getAttributeInstance(attribute)?.let { instance ->
                instance.removeModifier(modifier)
                instance.addModifier(modifier)
            }
        }
    }

    override fun clearModifiers(uuid: UUID) {
        for (instance in attributes.values) {
            instance.removeModifier(uuid)
        }
    }

    override fun clearAllModifiers() {
        for (instance in attributes.values) {
            instance.removeModifiers()
        }
    }

    override fun assignValues(other: AttributeMap) {
        require(other is PlayerAttributeMap) { "Can't assign values from non-PlayerAttributeMap" }
        for (instance in other.attributes.values) {
            getAttributeInstance(instance.attribute)?.replace(instance)
        }
    }
}
