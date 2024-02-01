package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import java.util.*

/**
 * A combination of an [Attribute] and related [AttributeModifier]s.
 *
 * This should be directly linked to a certain entity, e.g., a player.
 */
class AttributeInstance(
    val attribute: Attribute,
) {
    private val modifiersById: MutableMap<UUID, AttributeModifier> = Object2ObjectArrayMap()
    private val modifiersByOperation: MutableMap<Operation, MutableSet<AttributeModifier>> = EnumMap(Operation::class.java)
    private var dirty: Boolean = true
    private var baseValue: Double = attribute.defaultValue
    private var cachedValue: Double = 0.0

    fun getDescriptionId(): String = attribute.descriptionId

    fun getBaseValue(): Double = baseValue
    fun setBaseValue(baseValue: Double) {
        if (baseValue != this.baseValue) {
            this.baseValue = baseValue
            setDirty()
        }
    }

    private fun setDirty() {
        dirty = true
    }

    fun getValue(): Double {
        if (dirty) {
            cachedValue = calculateValue()
            dirty = false
        }
        return cachedValue
    }

    private fun calculateValue(): Double {
        var x: Double = getBaseValue()
        getModifierOrEmpty(Operation.ADDITION).forEach { x += it.amount }
        var y: Double = x
        getModifierOrEmpty(Operation.MULTIPLY_BASE).forEach { y += x * it.amount }
        getModifierOrEmpty(Operation.MULTIPLY_TOTAL).forEach { y *= 1.0 + it.amount }
        return this.attribute.sanitizeValue(y)
    }

    fun getModifierOrEmpty(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOperation.getOrDefault(operation, Collections.emptySet())
    }

    fun getModifiers(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOperation.computeIfAbsent(operation) { HashSet() }
    }

    fun getModifiers(): Set<AttributeModifier> {
        return modifiersById.values.toSet()
    }

    fun getModifier(uuid: UUID): AttributeModifier? {
        return modifiersById[uuid]
    }

    fun hasModifier(modifier: AttributeModifier): Boolean {
        return modifiersById.containsKey(modifier.id)
    }

    fun addModifier(modifier: AttributeModifier) {
        val attributeModifier: AttributeModifier? = modifiersById.putIfAbsent(modifier.id, modifier)
        require(attributeModifier == null) { "Modifier is already applied on this attribute!" }
        getModifiers(modifier.operation).add(modifier)
        setDirty()
    }

    fun removeModifier(modifier: AttributeModifier) {
        getModifiers(modifier.operation).remove(modifier)
        modifiersById.remove(modifier.id)
        setDirty()
    }

    fun removeModifier(uuid: UUID) {
        getModifier(uuid)?.let { removeModifier(it) }
        setDirty()
    }

    fun removeModifiers() {
        modifiersById.clear()
        modifiersByOperation.clear()
        setDirty()
    }

    /**
     * Replace the states of this instance with the [other]'s.
     */
    fun replace(other: AttributeInstance) {
        baseValue = other.getBaseValue()
        modifiersById.clear()
        modifiersById.putAll(other.modifiersById)
        modifiersByOperation.clear()
        modifiersByOperation.putAll(other.modifiersByOperation)
        setDirty()
    }
}