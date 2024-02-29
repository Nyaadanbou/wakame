package cc.mewcraft.wakame.attribute.base

import cc.mewcraft.wakame.util.toBukkit
import com.google.common.collect.Multimap
import org.bukkit.entity.LivingEntity
import java.util.*

/**
 * This class contains attribute information about a player.
 *
 * States of this object should be periodically updated by
 * [AttributeAccessor].
 *
 * Not thread-safe.
 */
class AttributeMap(
    private val defaultSupplier: AttributeSupplier,
    private val entity: LivingEntity,
) {
    private val attributes: MutableMap<Attribute, AttributeInstance> = HashMap()

    operator fun get(attribute: Attribute): AttributeInstance? {
        return getAttributeInstance(attribute)
    }

    fun getAttributeInstance(attribute: Attribute): AttributeInstance? {
        // Kotlin 对于 Map.computeIfAbsent 中 lambda 的返回值做了非空要求
        // 因此这里手动实现了一遍 computeIfAbsent 以还原在 Java 下的行为
        // See: https://youtrack.jetbrains.com/issue/KT-10982
        val oldValue = attributes[attribute]
        if (oldValue == null) {
            if (Attributes.isVanilla(attribute)) {
                val bukkitAttribute = attribute.toBukkit()
                val bukkitInstance = entity.getAttribute(bukkitAttribute)
                if (bukkitInstance != null) {
                    val instance = VanillaAttributeInstanceWrapper(bukkitInstance)
                    attributes[attribute] = instance
                    return instance
                }
                throw IllegalArgumentException("Can't find vanilla attribute instance for $attribute")
            }

            val newValue = defaultSupplier.createAttributeInstance(attribute)
            if (newValue != null) {
                attributes[attribute] = newValue
                return newValue
            }
        }
        return oldValue
    }

    fun getAttributeInstanceOrThrow(attribute: Attribute): AttributeInstance {
        return requireNotNull(getAttributeInstance(attribute)) { "Can't find attribute instance for attribute $attribute" }
    }

    fun registerAttribute(attributeBase: Attribute) {
        if (Attributes.isVanilla(attributeBase)) {
            val bukkitAttribute = attributeBase.toBukkit()
            entity.registerAttribute(bukkitAttribute)
            attributes[attributeBase] = VanillaAttributeInstanceWrapper(entity.getAttribute(bukkitAttribute)!!)
            return
        }

        val attributeModifiable = WakameAttributeInstance(attributeBase)
        attributes[attributeBase] = attributeModifiable
    }

    fun hasAttribute(attribute: Attribute): Boolean {
        return attributes.containsKey(attribute)
    }

    fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        return attributes[attribute]?.getModifier(uuid) != null || defaultSupplier.hasModifier(attribute, uuid)
    }

    fun getValue(attribute: Attribute): Double {
        return attributes[attribute]?.getValue() ?: defaultSupplier.getValue(attribute)
    }

    fun getBaseValue(attribute: Attribute): Double {
        return attributes[attribute]?.getBaseValue() ?: defaultSupplier.getBaseValue(attribute)
    }

    fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        return attributes[attribute]?.getModifier(uuid)?.amount ?: defaultSupplier.getModifierValue(attribute, uuid)
    }

    fun removeAttributeModifiers(attributeModifiers: Multimap<out Attribute, AttributeModifier>) {
        for ((attribute, modifiers) in attributeModifiers.asMap()) {
            attributes[attribute]?.let {
                modifiers.forEach(it::removeModifier)
            }
        }
    }

    fun addAttributeModifiers(attributeModifiers: Multimap<out Attribute, AttributeModifier>) {
        attributeModifiers.forEach { attribute, modifier ->
            getAttributeInstance(attribute)?.let { instance ->
                instance.removeModifier(modifier)
                instance.addModifier(modifier)
            }
        }
    }

    fun clearAllModifiers() {
        for (instance in attributes.values) {
            instance.removeModifiers()
        }
    }

    fun assignValues(other: AttributeMap) {
        for (instance in other.attributes.values) {
            getAttributeInstance(instance.attribute)?.replace(instance)
        }
    }
}
