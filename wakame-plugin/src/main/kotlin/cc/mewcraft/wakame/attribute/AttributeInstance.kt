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
     * 用于创建实例。实例将绑定到 [attributable].
     *
     * **副作用** - 会依据情况修改 [attributable] 的状态。
     *
     * @param attribute
     * @param attributable 世界状态中需要创建 [AttributeInstance] 的对象
     * @param registerVanilla 是否在世界状态中为 [attributable] 注册属性
     */
    fun createInstance(attribute: Attribute, attributable: Attributable, registerVanilla: Boolean): AttributeInstance {
        return if (attribute.vanilla) {
            // 关于 Attribute#vanilla 的解释 -
            // 设计上，如果这个 Attribute 是基于原版的（例如移速），
            // 那么我们的 AttributeInstance 在实现上则是对应原版
            // AttributeInstance 的代理 —— 也就是说，函数的调用
            // 会重定向到原版 AttributeInstance 对应的函数上

            // 从 Attributable 中获取要被封装的 BukkitAttributeInstance
            val bukkitInst = run {
                val ret: BukkitAttributeInstance?
                val instanceOrNull = attributable.getAttribute(attribute.toBukkit())
                if (instanceOrNull == null) {
                    if (registerVanilla) {
                        // 仅当该 Attributable 没有该属性，
                        // 并且 registerVanilla 为 true 时
                        // 我们才真的新注册该属性

                        // 这将产生副作用，会直接改变 Attributable 的世界状态
                        // 这部分没有详细的 API 文档，但我们自己总结一下，就是
                        // - 当该属性本来就存在时，它会覆盖原有的
                        attributable.registerAttribute(attribute.toBukkit())
                    } else {
                        // 该 Attributable 不存在该原版属性，
                        // 然而用户并没有指定允许注册新的属性
                        throw IllegalArgumentException("Can't find vanilla attribute instance for attribute $attribute")
                    }
                    ret = attributable.getAttribute(attribute.toBukkit())!!
                } else {
                    ret = instanceOrNull
                }

                ret
            }

            VanillaAttributeInstance(bukkitInst)
        } else {
            WakameAttributeInstance(attribute)
        }
    }
}

/**
 * This class shares common code to implement [AttributeInstance].
 */
private sealed class SimpleAttributeInstance : AttributeInstance {
    private val modifiersByUuid: MutableMap<UUID, AttributeModifier> = Object2ObjectArrayMap()
    private val modifiersByOperation: MutableMap<Operation, MutableSet<AttributeModifier>> = enumMap()
    private var dirty: Boolean = true
    private var baseValue: Double
    private var cachedValue: Double = 0.0

    constructor(attribute: Attribute) {
        baseValue = attribute.defaultValue // initially set the baseValue to the attribute.defaultValue
    }

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
        require(modifiersByUuid.putIfAbsent(modifier.id, modifier) == null) { "$modifier is already applied on this attribute!" }
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
        if (other is SimpleAttributeInstance) {
            // Optimizations: save several instructions for known types
            baseValue = other.baseValue
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
 * A prototype of [AttributeInstance].
 *
 * It's used to create new instances of [VanillaAttributeInstance] and
 * [WakameAttributeInstance].
 */
private class ProtoAttributeInstance(
    override val attribute: Attribute,
) : SimpleAttributeInstance(attribute)

/**
 * A wakame [AttributeInstance].
 *
 * This class represents the concrete attribute instance in our own system.
 */
private class WakameAttributeInstance(
    override val attribute: Attribute,
) : SimpleAttributeInstance(attribute)

/**
 * A vanilla [AttributeInstance].
 *
 * This class essentially wraps an object of [BukkitAttributeInstance] and
 * redirects all the function calls to corresponding those of
 * [BukkitAttributeInstance].
 */
private class VanillaAttributeInstance
/**
 * 该构造器用于封装世界状态。
 */
constructor(
    private val handle: BukkitAttributeInstance, // 封装的对象
) : AttributeInstance {
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
        // 如果玩家手持 wakame 物品，并且 wakame 物品上有增加最大生命值的属性（原版属性），
        // 那么当玩家先离线，然后再上线，后台会抛异常：Modifier is already applied on this attribute!
        // 这是因为玩家下线时，并不会触发移除物品属性的逻辑，导致属性被永久保存到 NBT 了
        // 解决办法：addTransientModifier
        handle.addTransientModifier(modifier.toBukkit())
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
        this.getModifiers().forEach { removeModifier(it) }
        other.getModifiers().forEach { addModifier(it) }
    }
}
