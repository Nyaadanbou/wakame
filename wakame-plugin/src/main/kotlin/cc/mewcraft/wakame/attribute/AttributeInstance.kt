package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import com.google.common.collect.ImmutableSet
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder
import com.google.common.collect.SetMultimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.bukkit.attribute.Attributable
import java.util.UUID
import org.bukkit.attribute.AttributeInstance as BukkitAttributeInstance

/**
 * 属性实例 ([AttributeInstance]) 本质上是一个用于数值计算的容器, 里面包含了一个
 * [Attribute] 和多个与之关联的 [AttributeModifier]. 每一个 [AttributeModifier]
 * 在概念上都是对 [Attribute] 的修饰, 会影响 [AttributeInstance] 的数值计算结果.
 *
 * **Caution:** This should be directly linked to a certain living entity,
 * e.g. a player, a zombie, etc. Any changes on this instance
 * should reflect on the linked entity in real time.
 */
sealed interface AttributeInstance : AttributeInstanceSnapshotable {
    /**
     * The [Attribute] in this [AttributeInstance].
     *
     * The object serves as a "prototype", where we might take the [Attribute.defaultValue]
     * as the return value of [getBaseValue], or we just read the `baseValue` stored in
     * the [AttributeInstance] object.
     */
    val attribute: Attribute
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

/**
 * 代表一个不可变的属性实例.
 */
sealed interface IntangibleAttributeInstance : AttributeInstanceSnapshotable {
    val attribute: Attribute
    fun getValue(): Double
    fun getBaseValue(): Double
    fun getModifier(uuid: UUID): AttributeModifier?
    fun getModifiers(): Set<AttributeModifier>
    fun hasModifier(modifier: AttributeModifier): Boolean
}

/**
 * 代表一个属性实例的快照 (支持读/写), 用于临时的数值储存和计算.
 */
sealed interface AttributeInstanceSnapshot {
    val attribute: Attribute
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
    fun toImmutable(): IntangibleAttributeInstance
}

/**
 * 代表一个可以产生 [AttributeInstanceSnapshot] 的对象.
 */
sealed interface AttributeInstanceSnapshotable {
    /**
     * 根据当前状态创建一个 [AttributeInstanceSnapshot].
     */
    fun getSnapshot(): AttributeInstanceSnapshot
}

/**
 * 用于创建各种类型的 [AttributeInstance].
 */
object AttributeInstanceFactory {
    /**
     * 用于创建原型, 以构建 [AttributeSupplier].
     */
    fun createPrototype(attribute: Attribute): AttributeInstance {
        return ProtoAttributeInstance(AttributeInstanceDelegation(attribute))
    }

    /**
     * 用于创建一个独立的 [AttributeInstance].
     *
     * 本函数返回的实例不与任何主体绑定, 是完全独立的数据结构.
     * 实例本身也不会自动更新自己的任何数据, 除非你显式的这么做.
     *
     * @param attribute
     */
    fun createInstance(attribute: Attribute): AttributeInstance {
        return WakameAttributeInstance(AttributeInstanceDelegation(attribute))
    }

    /**
     * 用于创建一个标准的 [AttributeInstance].
     *
     * 本函数返回的实例将自动绑定到 [attributable] 上, 将对 [attributable] 产生副作用.
     * 也就是说, 任何对本实例的修改 (例如增加最大生命值) 都会实时反应到 [attributable] 上.
     *
     * @param attribute
     * @param attributable 世界状态中需要创建 [AttributeInstance] 的对象
     * @param registerVanilla 是否在世界状态中为 [attributable] 注册属性
     */
    fun createInstance(attribute: Attribute, attributable: Attributable, registerVanilla: Boolean): AttributeInstance {
        return if (!attribute.vanilla) {
            // 是我们自己的萌芽属性, 直接创建实例即可
            WakameAttributeInstance(AttributeInstanceDelegation(attribute))
        } else {
            // 关于 Attribute#vanilla 的解释:
            // 设计上, 如果这个 Attribute 是基于原版的(比如最大生命值),
            // 那么我们的 AttributeInstance 在实现上则必须是对应原版
            // AttributeInstance 的 proxy —— 也就是说, 所有函数调用
            // 必须重定向到原版 AttributeInstance 的实例上

            // 从 Attributable 中获取要被封装的 BukkitAttributeInstance
            val handle = run {
                val ret: BukkitAttributeInstance?
                val bukkitAttribute = attribute.toBukkit()
                val bukkitAttributeInstance = attributable.getAttribute(bukkitAttribute)
                if (bukkitAttributeInstance != null) {
                    ret = bukkitAttributeInstance
                } else {
                    if (registerVanilla) {
                        // 仅当该 Attributable 没有该属性, 并且 registerVanilla 为 true 时, 我们才真的新注册该属性

                        // 这将产生副作用, 会直接改变 Attributable 的世界状态.
                        // 这部分没有详细的 API 文档, 但我们自己总结一下就是:
                        // 当该属性本来就存在时, 它会覆盖原有的.
                        attributable.registerAttribute(bukkitAttribute)
                    } else {
                        // 该 Attributable 不存在该原版属性,
                        // 然而用户并没有指定允许注册新的属性.
                        throw IllegalArgumentException("Can't find vanilla attribute instance for attribute $attribute")
                    }
                    ret = attributable.getAttribute(bukkitAttribute)!!
                }

                ret
            }

            VanillaAttributeInstance(handle)
        }
    }
}


/* Implementations */


/**
 * 该类提供了各种属性实例的核心逻辑.
 */
private class AttributeInstanceDelegation(
    val attribute: Attribute,
) : Cloneable {
    val modifiersById: Object2ObjectArrayMap<UUID, AttributeModifier> = Object2ObjectArrayMap()
    val modifiersByOp: SetMultimap<Operation, AttributeModifier> = SetMultimapBuilder.enumKeys(Operation::class.java).hashSetValues().build()
    var dirty: Boolean = true
    @get:JvmName("baseValue")
    @set:JvmName("baseValue")
    var baseValue: Double = attribute.defaultValue // initially set the baseValue to the attribute.defaultValue
    var cachedValue: Double = 0.0

    fun getBaseValue(): Double {
        return baseValue
    }

    fun setBaseValue(baseValue: Double) {
        if (baseValue != this.baseValue) {
            this.baseValue = baseValue
            this.dirty = true
        }
    }

    fun getValue(): Double {
        if (dirty) {
            cachedValue = calculateValue()
            dirty = false
        }
        return cachedValue
    }

    fun getModifiers(): Set<AttributeModifier> {
        return ImmutableSet.copyOf(modifiersById.values)
    }

    fun getModifier(uuid: UUID): AttributeModifier? {
        return modifiersById[uuid]
    }

    fun hasModifier(modifier: AttributeModifier): Boolean {
        return modifiersById.containsKey(modifier.id)
    }

    fun addModifier(modifier: AttributeModifier) {
        // FIXME 修复重复添加 AttributeModifier 的问题
        //  目前如果同一个物品上, 同一个 Attribute, 有多个不同的 AttributeModifier,
        //  那么这里的 alreadyExists 就会返回 true, 从而导致 AttributeModifier 产生“重复”问题.
        val alreadyExists = modifiersById.putIfAbsent(modifier.id, modifier) != null
        if (alreadyExists) {
            AttributeSupport.LOGGER.warn("$modifier is already applied on this attribute!")
        }
        getModifiersOrCreateEmpty(modifier.operation).add(modifier)
        dirty = true
    }

    fun removeModifier(modifier: AttributeModifier) {
        modifiersById.remove(modifier.id)
        getModifiersOrCreateEmpty(modifier.operation).remove(modifier)
        dirty = true
    }

    fun removeModifier(uuid: UUID) {
        getModifier(uuid)?.let { removeModifier(it) }
        dirty = true
    }

    fun removeModifiers() {
        modifiersById.clear()
        modifiersByOp.clear()
        dirty = true
    }

    fun replace(other: AttributeInstanceDelegation) {
        this.baseValue = other.baseValue
        this.modifiersById.clear()
        this.modifiersById.putAll(other.modifiersById)
        this.modifiersByOp.clear()
        this.modifiersByOp.putAll(other.modifiersByOp)
        this.dirty = true
    }

    public override fun clone(): AttributeInstanceDelegation {
        val ret = AttributeInstanceDelegation(this.attribute)
        ret.baseValue = this.baseValue
        ret.modifiersById.putAll(this.modifiersById)
        ret.modifiersByOp.putAll(this.modifiersByOp)
        ret.dirty = this.dirty
        return ret
    }

    private fun calculateValue(): Double {
        var x: Double = getBaseValue()
        getModifierOrReturnEmpty(Operation.ADD).forEach { x += it.amount }
        var y: Double = x
        getModifierOrReturnEmpty(Operation.MULTIPLY_BASE).forEach { y += x * it.amount }
        getModifierOrReturnEmpty(Operation.MULTIPLY_TOTAL).forEach { y *= 1.0 + it.amount }
        return this.attribute.sanitizeValue(y)
    }

    private fun getModifierOrReturnEmpty(operation: Operation): Set<AttributeModifier> {
        if (!modifiersByOp.containsKey(operation)) {
            return emptySet()
        }
        return modifiersByOp.get(operation)
    }

    private fun getModifiersOrCreateEmpty(operation: Operation): MutableSet<AttributeModifier> {
        return modifiersByOp.get(operation)
    }
}

/**
 * A prototype of [AttributeInstance].
 *
 * It's solely used to create new instances of [VanillaAttributeInstance] and
 * [WakameAttributeInstance], and should not be stored in the world state.
 */
private class ProtoAttributeInstance(
    val delegation: AttributeInstanceDelegation,
) : AttributeInstance {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getSnapshot(): AttributeInstanceSnapshot =
        AttributeInstanceSnapshotImpl(delegation.clone())

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun setBaseValue(baseValue: Double) =
        delegation.setBaseValue(baseValue)

    override fun getModifier(uuid: UUID): AttributeModifier? =
        delegation.getModifier(uuid)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun addModifier(modifier: AttributeModifier) =
        delegation.addModifier(modifier)

    override fun removeModifier(modifier: AttributeModifier) =
        delegation.removeModifier(modifier)

    override fun removeModifier(uuid: UUID) =
        delegation.removeModifier(uuid)

    override fun removeModifiers() =
        delegation.removeModifiers()

    override fun replace(other: AttributeInstance): Unit =
        throw UnsupportedOperationException("This operation is not supported for prototype instances.")
}

/**
 * A wakame [AttributeInstance].
 *
 * This class represents the concrete attribute instance in our own system.
 */
private class WakameAttributeInstance(
    val delegation: AttributeInstanceDelegation,
) : AttributeInstance {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getSnapshot(): AttributeInstanceSnapshot =
        AttributeInstanceSnapshotImpl(delegation.clone())

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun setBaseValue(baseValue: Double) =
        delegation.setBaseValue(baseValue)

    override fun getModifier(uuid: UUID): AttributeModifier? =
        delegation.getModifier(uuid)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun addModifier(modifier: AttributeModifier) =
        delegation.addModifier(modifier)

    override fun removeModifier(modifier: AttributeModifier) =
        delegation.removeModifier(modifier)

    override fun removeModifier(uuid: UUID) =
        delegation.removeModifier(uuid)

    override fun removeModifiers() =
        delegation.removeModifiers()

    override fun replace(other: AttributeInstance) {
        if (other is WakameAttributeInstance) {
            delegation.replace(other.delegation)
        } else {
            this.setBaseValue(other.getBaseValue())
            this.getModifiers().forEach { this.removeModifier(it) }
            other.getModifiers().forEach { this.addModifier(it) }
        }
    }
}

/**
 * A vanilla [AttributeInstance].
 *
 * This class essentially wraps an object of [BukkitAttributeInstance] and
 * redirects all the function calls to corresponding those of
 * [BukkitAttributeInstance].
 */
private class VanillaAttributeInstance(
    val handle: BukkitAttributeInstance, // 封装的世界状态中的对象
) : AttributeInstance {
    override val attribute: Attribute
        get() = handle.attribute.toNeko()

    override fun getSnapshot(): AttributeInstanceSnapshot {
        val delegation = AttributeInstanceDelegation(attribute)
        delegation.setBaseValue(getBaseValue())
        getModifiers().forEach { delegation.addModifier(it) }
        return AttributeInstanceSnapshotImpl(delegation)
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
        return handle.modifiers.mapTo(ObjectArraySet()) { it.toNeko() } // copy
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return handle.getModifier(modifier.id) != null
    }

    override fun addModifier(modifier: AttributeModifier) {
        // 如果玩家手持 wakame 物品, 并且 wakame 物品上有增加最大生命值的属性 (原版属性),
        // 那么当玩家先离线, 然后再上线, 后台会抛异常: “Modifier is already applied on this attribute!”
        // 这是因为玩家下线时, 并不会触发移除物品属性的逻辑, 导致属性被永久保存到 NBT 了.
        // 解决办法: addTransientModifier
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

private class IntangibleAttributeInstanceImpl(
    val delegation: AttributeInstanceDelegation,
) : IntangibleAttributeInstance {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getValue(): Double {
        return delegation.getValue()
    }

    override fun getBaseValue(): Double {
        return delegation.getBaseValue()
    }

    override fun getModifier(uuid: UUID): AttributeModifier? {
        return delegation.getModifier(uuid)
    }

    override fun getModifiers(): Set<AttributeModifier> {
        return delegation.getModifiers()
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return delegation.hasModifier(modifier)
    }

    override fun getSnapshot(): AttributeInstanceSnapshot {
        return AttributeInstanceSnapshotImpl(delegation.clone())
    }
}

private class AttributeInstanceSnapshotImpl(
    val delegation: AttributeInstanceDelegation,
) : AttributeInstanceSnapshot {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun setBaseValue(baseValue: Double) =
        delegation.setBaseValue(baseValue)

    override fun getModifier(uuid: UUID): AttributeModifier? =
        delegation.getModifier(uuid)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun addModifier(modifier: AttributeModifier) =
        delegation.addModifier(modifier)

    override fun removeModifier(modifier: AttributeModifier) =
        delegation.removeModifier(modifier)

    override fun removeModifier(uuid: UUID) =
        delegation.removeModifier(uuid)

    override fun removeModifiers() =
        delegation.removeModifiers()

    override fun toImmutable(): IntangibleAttributeInstance {
        return IntangibleAttributeInstanceImpl(delegation.clone())
    }
}
