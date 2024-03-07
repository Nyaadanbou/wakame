package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.util.toBukkit
import cc.mewcraft.wakame.util.toNeko
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.Collections
import java.util.EnumMap
import java.util.UUID
import org.bukkit.attribute.AttributeInstance as BukkitAttributeInstance

/**
 * A combination of an [Attribute] and related [AttributeModifier]s.
 *
 * This should be directly linked to a certain entity, e.g., a player.
 */
interface AttributeInstance {
    val attribute: Attribute

    fun getDescriptionId(): String

    fun getValue(): Double

    fun getBaseValue(): Double

    fun setBaseValue(baseValue: Double)

    fun getModifier(uuid: UUID): AttributeModifier?

    fun getModifierOrEmpty(operation: Operation): Set<AttributeModifier>

    fun getModifiers(operation: Operation): Set<AttributeModifier>

    fun getModifiers(): Set<AttributeModifier>

    fun hasModifier(modifier: AttributeModifier): Boolean

    fun addModifier(modifier: AttributeModifier)

    fun removeModifier(modifier: AttributeModifier)

    fun removeModifier(uuid: UUID)

    fun removeModifiers()

    fun replace(other: AttributeInstance)
}

internal class VanillaAttributeInstance(
    private val handle: BukkitAttributeInstance,
) : AttributeInstance {

    override val attribute: Attribute = handle.attribute.toNeko()

    override fun getDescriptionId(): String {
        return attribute.descriptionId
    }

    override fun getValue(): Double {
        return handle.value
    }

    override fun getBaseValue(): Double {
        return handle.baseValue
    }

    override fun setBaseValue(baseValue: Double) {
        handle.baseValue = baseValue
    }

    override fun getModifier(uuid: UUID): AttributeModifier? {
        return handle.modifiers
            .firstOrNull { it.uniqueId == uuid }
            ?.let { AttributeModifier(it.uniqueId, it.name, it.amount, it.operation.toNeko()) }
    }

    override fun getModifierOrEmpty(operation: Operation): Set<AttributeModifier> {
        return handle.modifiers
            .filter { it.operation == operation.toBukkit() }
            .mapTo(ObjectOpenHashSet()) { it.toNeko() }
    }

    override fun getModifiers(): Set<AttributeModifier> {
        return handle.modifiers.mapTo(HashSet()) { it.toNeko() }
    }

    override fun getModifiers(operation: Operation): Set<AttributeModifier> {
        return handle.modifiers
            .filter { it.operation == operation.toBukkit() }
            .mapTo(ObjectOpenHashSet()) { it.toNeko() }
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return handle.modifiers.any { it.toNeko() == modifier }
    }

    override fun addModifier(modifier: AttributeModifier) {
        handle.addModifier(modifier.toBukkit())
    }

    override fun removeModifier(modifier: AttributeModifier) {
        handle.removeModifier(modifier.toBukkit())
    }

    override fun removeModifier(uuid: UUID) {
        handle.modifiers
            .firstOrNull { it.uniqueId == uuid }
            ?.let { handle.removeModifier(it) }
    }

    override fun removeModifiers() {
        handle.modifiers.forEach { handle.removeModifier(it) }
    }

    override fun replace(other: AttributeInstance) {
        require(other is VanillaAttributeInstance) { "Can't replace with a different type of AttributeInstance" }
        handle.baseValue = other.getBaseValue()
        handle.modifiers.forEach { handle.removeModifier(it) }
        other.handle.modifiers.forEach { handle.addModifier(it) }
    }
}

internal class WakameAttributeInstance(
    override val attribute: Attribute,
) : AttributeInstance {
    private val modifiersById: MutableMap<UUID, AttributeModifier> = Object2ObjectArrayMap()
    private val modifiersByOperation: MutableMap<Operation, MutableSet<AttributeModifier>> = EnumMap(Operation::class.java)
    private var dirty: Boolean = true
    private var baseValue: Double = attribute.defaultValue
    private var cachedValue: Double = 0.0

    override fun getDescriptionId(): String {
        return attribute.descriptionId
    }

    override fun getBaseValue(): Double {
        return baseValue
    }

    override fun setBaseValue(baseValue: Double) {
        if (baseValue != this.baseValue) {
            this.baseValue = baseValue
            setDirty()
        }
    }

    private fun setDirty() {
        dirty = true
    }

    override fun getValue(): Double {
        if (dirty) {
            cachedValue = calculateValue()
            dirty = false
        }
        return cachedValue
    }

    private fun calculateValue(): Double {
        var x: Double = getBaseValue()
        getModifierOrEmpty(Operation.ADD).forEach { x += it.amount }
        var y: Double = x
        getModifierOrEmpty(Operation.MULTIPLY_BASE).forEach { y += x * it.amount }
        getModifierOrEmpty(Operation.MULTIPLY_TOTAL).forEach { y *= 1.0 + it.amount }
        return this.attribute.sanitizeValue(y)
    }

    override fun getModifierOrEmpty(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOperation.getOrDefault(operation, Collections.emptySet())
    }

    override fun getModifiers(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOperation.computeIfAbsent(operation) { HashSet() }
    }

    override fun getModifiers(): Set<AttributeModifier> {
        return modifiersById.values.toSet()
    }

    override fun getModifier(uuid: UUID): AttributeModifier? {
        return modifiersById[uuid]
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return modifiersById.containsKey(modifier.id)
    }

    override fun addModifier(modifier: AttributeModifier) {
        val attributeModifier: AttributeModifier? = modifiersById.putIfAbsent(modifier.id, modifier)
        require(attributeModifier == null) { "Modifier is already applied on this attribute!" }
        getModifiers(modifier.operation).add(modifier)
        setDirty()
    }

    override fun removeModifier(modifier: AttributeModifier) {
        getModifiers(modifier.operation).remove(modifier)
        modifiersById.remove(modifier.id)
        setDirty()
    }

    override fun removeModifier(uuid: UUID) {
        getModifier(uuid)?.let { removeModifier(it) }
        setDirty()
    }

    override fun removeModifiers() {
        modifiersById.clear()
        modifiersByOperation.clear()
        setDirty()
    }

    /**
     * Replace the states of this instance with the [other]'s.
     */
    override fun replace(other: AttributeInstance) {
        require(other is WakameAttributeInstance) { "Can't replace with a different type of AttributeInstance" }

        baseValue = other.getBaseValue()
        modifiersById.clear()
        modifiersById.putAll(other.modifiersById)
        modifiersByOperation.clear()
        modifiersByOperation.putAll(other.modifiersByOperation)
        setDirty()
    }
}