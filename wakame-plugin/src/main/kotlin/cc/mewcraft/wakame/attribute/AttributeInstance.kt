package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.collections.enumMap
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.attribute.Attributable
import java.util.Collections
import java.util.UUID
import org.bukkit.attribute.AttributeInstance as BukkitAttributeInstance

/**
 * Represents a combination of an [Attribute] with zero or more
 * [AttributeModifier]s owned by the [Attribute].
 *
 * This should be directly linked to a certain living entity,
 * e.g. a player, a zombie, etc.
 */
interface AttributeInstance {
    /**
     * The [Attribute] in this [AttributeInstance].
     *
     * The object serves as a "prototype", where we might take the [Attribute.defaultValue]
     * as the return value of [getBaseValue], or we just read the `baseValue` stored in
     * the [AttributeInstance] object.
     */
    val attribute: Attribute

    fun getDescriptionId(): String
    fun getValue(): Double
    fun getBaseValue(): Double
    fun setBaseValue(baseValue: Double)
    fun getModifier(uuid: UUID): AttributeModifier?
    fun getModifiers(): Set<AttributeModifier>
    fun hasModifier(modifier: AttributeModifier): Boolean
    fun addModifier(modifier: AttributeModifier)
    fun removeModifier(modifier: AttributeModifier)
    fun removeModifier(uuid: UUID)
    fun removeModifiers()

    /**
     * Replace the states of this instance with the [other]'s.
     */
    fun replace(other: AttributeInstance)
}

internal object AttributeInstanceFactory {
    /**
     * 用于创建原型。原型不应该放在世界状态里。
     */
    fun createPrototype(attribute: Attribute): AttributeInstance {
        return ProtoAttributeInstance(attribute)
    }

    /**
     * 用于创建实例。实例必须反映在世界状态里。
     *
     * **副作用** - 会依据情况修改 [attributable] 的状态。
     *
     * @param attribute
     * @param attributable 世界状态里的对象
     */
    fun createInstance(attribute: Attribute, attributable: Attributable): AttributeInstance {
        return if (attribute.vanilla) {
            // 设计上，如果这个 Attribute 是原版的（例如移动速度），
            // 那么我们的 AttributeInstance 在实现上则是对应原版
            // AttributeInstance 的封装 —— 也就是说，函数的调用
            // 会重定向到原版 AttributeInstance 对应的函数上

            // 副作用 - 注册一下 AttributeInstance
            attributable.registerAttribute(attribute.toBukkit())

            // 从 Attributable 中获取要被封装的 AttributeInstance
            val handle = requireNotNull(attributable.getAttribute(attribute.toBukkit())) {
                "Can't find vanilla attribute instance for attribute $attribute"
            }

            VanillaAttributeInstance(handle)
        } else {
            WakameAttributeInstance(attribute)
        }
    }
}

/**
 * A prototype of [AttributeInstance].
 *
 * It's used to create new instances of [VanillaAttributeInstance] and [WakameAttributeInstance].
 */
private class ProtoAttributeInstance(
    override val attribute: Attribute,
) : AttributeInstance {

    private val modifiersByUuid: MutableMap<UUID, AttributeModifier> = Object2ObjectArrayMap()
    private val modifiersByOperation: MutableMap<Operation, MutableSet<AttributeModifier>> = enumMap()
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

    override fun getValue(): Double {
        if (dirty) {
            cachedValue = calculateValue()
            dirty = false
        }
        return cachedValue
    }

    override fun getModifiers(): Set<AttributeModifier> {
        return ImmutableSet.copyOf(modifiersByUuid.values)
    }

    override fun getModifier(uuid: UUID): AttributeModifier? {
        return modifiersByUuid[uuid]
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return modifiersByUuid.containsKey(modifier.id)
    }

    override fun addModifier(modifier: AttributeModifier) {
        requireNotNull(modifiersByUuid.putIfAbsent(modifier.id, modifier)) { "$modifier is already applied on this attribute!" }
        getModifiers0(modifier.operation).add(modifier)
        setDirty()
    }

    override fun removeModifier(modifier: AttributeModifier) {
        modifiersByUuid.remove(modifier.id)
        getModifiers0(modifier.operation).remove(modifier)
        setDirty()
    }

    override fun removeModifier(uuid: UUID) {
        getModifier(uuid)?.let { removeModifier(it) }
        setDirty()
    }

    override fun removeModifiers() {
        modifiersByUuid.clear()
        modifiersByOperation.clear()
        setDirty()
    }

    override fun replace(other: AttributeInstance) {
        if (other is ProtoAttributeInstance) {
            // Optimizations: save several instructions for known types
            baseValue = other.getBaseValue()
            modifiersByUuid.clear()
            modifiersByUuid.putAll(other.modifiersByUuid)
            modifiersByOperation.clear()
            modifiersByOperation.putAll(other.modifiersByOperation)
            setDirty()
        } else {
            setBaseValue(other.getBaseValue())
            getModifiers().forEach { removeModifier(it) }
            other.getModifiers().forEach { addModifier(it) }
        }
    }

    private fun setDirty() {
        dirty = true
    }

    private fun calculateValue(): Double {
        fun getModifierOrEmpty(operation: Operation): MutableSet<AttributeModifier> {
            return modifiersByOperation.getOrDefault(operation, Collections.emptySet())
        }

        var x: Double = getBaseValue()
        getModifierOrEmpty(Operation.ADD).forEach { x += it.amount }
        var y: Double = x
        getModifierOrEmpty(Operation.MULTIPLY_BASE).forEach { y += x * it.amount }
        getModifierOrEmpty(Operation.MULTIPLY_TOTAL).forEach { y *= 1.0 + it.amount }
        return this.attribute.sanitizeValue(y)
    }

    private fun getModifiers0(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOperation.computeIfAbsent(operation) { ObjectOpenHashSet() }
    }
}

/**
 * A wakame [AttributeInstance].
 */
private class WakameAttributeInstance(
    override val attribute: Attribute,
) : AttributeInstance by ProtoAttributeInstance(attribute)

/**
 * A special [AttributeInstance] that wraps an object of [BukkitAttributeInstance].
 *
 * All the function calls are redirected to the wrapped [BukkitAttributeInstance].
 */
private class VanillaAttributeInstance : AttributeInstance {
    /**
     * 封装的对象。
     */
    private val handle: BukkitAttributeInstance

    /**
     * 该构造器用于封装世界状态。
     */
    constructor(handle: BukkitAttributeInstance) {
        this.handle = handle

        // also set the value in the world state
        setBaseValue(attribute.defaultValue)
    }

    override val attribute: Attribute
        get() = handle.attribute.toNeko()

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
        return handle.getModifier(uuid)?.toNeko()
    }

    override fun getModifiers(): Set<AttributeModifier> {
        return handle.modifiers.mapTo(ObjectOpenHashSet()) { it.toNeko() } // copy
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return handle.getModifier(modifier.id) != null
    }

    override fun addModifier(modifier: AttributeModifier) {
        handle.addModifier(modifier.toBukkit())
    }

    override fun removeModifier(modifier: AttributeModifier) {
        handle.removeModifier(modifier.toBukkit())
    }

    override fun removeModifier(uuid: UUID) {
        handle.removeModifier(uuid)
    }

    override fun removeModifiers() {
        handle.modifiers.forEach { removeModifier(it.uniqueId) }
    }

    override fun replace(other: AttributeInstance) {
        setBaseValue(other.getBaseValue())
        getModifiers().forEach { removeModifier(it) }
        other.getModifiers().forEach { addModifier(it) }
    }
}
