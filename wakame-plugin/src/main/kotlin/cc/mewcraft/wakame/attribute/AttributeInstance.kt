package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.collections.enumMap
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.util.AttributeInstanceSupplier
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
    fun replace(other: AttributeInstance)
}

internal object AttributeInstanceFactory {
    /**
     * 用于创建原型。原型不应该放在世界状态里。
     */
    fun createPrototype(attribute: Attribute): AttributeInstance {
        return if (attribute.vanilla) {
            VanillaAttributeInstance(attribute)
        } else {
            WakameAttributeInstance(attribute)
        }
    }

    /**
     * 用于创建实例。实例必须反映在世界状态里。
     *
     * ## 副作用
     *
     * 会依据情况修改 [attributable] 的状态。
     *
     * @param attribute
     * @param attributable 实例的状态源
     */
    fun createInstance(attribute: Attribute, attributable: Attributable): AttributeInstance {
        return if (attribute.vanilla) {
            // 设计上，如果这个 Attribute 是原版的（例如移动速度），
            // 那么我们的 AttributeInstance 在实现上则是对应原版
            // AttributeInstance 的封装 —— 也就是说，函数的调用
            // 会重定向到原版 AttributeInstance 对应的函数上

            // 需要注册一下 AttributeInstance
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

internal class VanillaAttributeInstance : AttributeInstance {
    /**
     * 封装的对象。
     */
    private val handle: BukkitAttributeInstance

    /**
     * 主构造器用于封装玩家的状态。
     */
    internal constructor(handle: BukkitAttributeInstance) {
        this.handle = handle
        setBaseValue(attribute.defaultValue)
    }

    /**
     * 该构造器仅用于构建原型，不要用于封装玩家的状态！
     */
    internal constructor(attribute: Attribute) : this(
        AttributeInstanceSupplier.createInstance(attribute.toBukkit())
    )

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
        require(other.attribute.vanilla) { "Can't replace with a non-vanilla AttributeInstance" }
        setBaseValue(other.getBaseValue())
        getModifiers().forEach { removeModifier(it) }
        other.getModifiers().forEach { addModifier(it) }
    }
}

internal class WakameAttributeInstance : AttributeInstance {
    constructor(attribute: Attribute) {
        this.attribute = attribute
        this.modifiersById = Object2ObjectArrayMap()
        this.modifiersByOperation = enumMap()
        this.baseValue = attribute.defaultValue
    }

    private val modifiersById: MutableMap<UUID, AttributeModifier>
    private val modifiersByOperation: MutableMap<Operation, MutableSet<AttributeModifier>>
    private var dirty: Boolean = true
    private var baseValue: Double
    private var cachedValue: Double = 0.0

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

    private fun getModifiers(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOperation.computeIfAbsent(operation) { ObjectOpenHashSet() }
    }

    override val attribute: Attribute

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
        return ImmutableSet.copyOf(modifiersById.values)
    }

    override fun getModifier(uuid: UUID): AttributeModifier? {
        return modifiersById[uuid]
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return modifiersById.containsKey(modifier.id)
    }

    override fun addModifier(modifier: AttributeModifier) {
        val attributeModifier = modifiersById.putIfAbsent(modifier.id, modifier)
        require(attributeModifier == null) { "$modifier is already applied on this attribute!" }
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