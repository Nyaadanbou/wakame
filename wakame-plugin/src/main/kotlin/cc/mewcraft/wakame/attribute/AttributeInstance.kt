package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier.*
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.objects.*
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attributable
import org.bukkit.entity.Player
import org.jetbrains.annotations.VisibleForTesting
import xyz.xenondevs.commons.collections.enumMap
import java.lang.ref.WeakReference
import java.util.EnumMap
import org.bukkit.attribute.AttributeInstance as BukkitAttributeInstance

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
    fun createDataInstance(attribute: Attribute): AttributeInstance {
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
    fun createLiveInstance(attribute: Attribute, attributable: Attributable, registerVanilla: Boolean): AttributeInstance {
        if (!attribute.vanilla) {
            // 是我们自己的萌芽属性, 直接创建实例即可
            return WakameAttributeInstance(AttributeInstanceDelegation(attribute))
        }

        // 关于 Attribute#vanilla 的解释:
        // 设计上, 如果这个 Attribute 是基于原版的(比如: generic.max_health),
        // 那么我们的 AttributeInstance 在实现上则必须是对应原版
        // AttributeInstance 的代理 —— 也就是说, 所有函数调用
        // 必须重定向到原版 AttributeInstance 的实例上.

        val handle = run {
            val bukkitAttribute = attribute.toBukkit()
            val bukkitAttributeInstance = attributable.getAttribute(bukkitAttribute)
            if (bukkitAttributeInstance != null) {
                // 如果世界状态中已经存在该属性, 那么我们直接返回它.
                return@run bukkitAttributeInstance
            }

            if (registerVanilla) {
                // 仅当 registerVanilla 为 true 时, 我们才新注册该属性.
                // 这将产生副作用, 会直接改变 Attributable 的世界状态!!!
                // 这部分没有 API 文档, 但查看 CraftBukkit 源码就会发现:
                // 当该属性本来就存在于 Attributable 时, 它会覆盖原有的.
                attributable.registerAttribute(bukkitAttribute)
            } else {
                // 该 Attributable 不存在该原版属性, 然而用户并没有指定允许注册新的属性.
                throw IllegalArgumentException("Can't find vanilla attribute instance for attribute $attribute")
            }
            return@run attributable.getAttribute(bukkitAttribute)!!
        }

        return VanillaAttributeInstance(handle, attributable as? Player)
    }
}


/* Implementations */


/**
 * 该类提供了各种属性实例的核心逻辑.
 */
private class AttributeInstanceDelegation(
    val attribute: Attribute,
    private var baseValue: Double = attribute.defaultValue,
    private var modifiersById: Object2ObjectArrayMap<Key, AttributeModifier> = Object2ObjectArrayMap(),
    private var modifiersByOp: EnumMap<Operation, Object2ObjectOpenHashMap<Key, AttributeModifier>> = enumMap(),
    private var cachedValue: Double = 0.0,
    private var dirty: Boolean = true,

    /**
     * 用于实现 copy-on-write.
     * 当为 false 时, 表示拥有所有权, 可以直接修改数据.
     * 当为 true 时, 表示没有所有权, 必须先复制再修改.
     */
    private var copyOnWrite: Boolean = false,
) {

    fun getBaseValue(): Double {
        return baseValue
    }

    fun setBaseValue(baseValue: Double) {
        ensureDataOwnership()
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

    @VisibleForTesting
    fun getModifiers(operation: Operation): MutableMap<Key, AttributeModifier> {
        return modifiersByOp.computeIfAbsent(operation) { Object2ObjectOpenHashMap() }
    }

    fun getModifier(id: Key): AttributeModifier? {
        return modifiersById[id]
    }

    fun hasModifier(modifier: AttributeModifier): Boolean {
        return modifiersById.containsKey(modifier.id)
    }

    fun addModifier(modifier: AttributeModifier) {
        // FIXME 因为尚未实现 WakameAttributeInstance 的持久化, 所以暂时不支持添加持久化的 modifier
        this.addTransientModifier(modifier)
    }

    fun addTransientModifier(modifier: AttributeModifier) {
        ensureDataOwnership()
        if (modifiersById.putIfAbsent(modifier.id, modifier) != null) {
            AttributeSupport.LOGGER.warn("$modifier is already applied on this attribute (same id)")
            return
        }
        if (getModifiers(modifier.operation).putIfAbsent(modifier.id, modifier) != null) {
            AttributeSupport.LOGGER.warn("$modifier is already applied on this attribute (same operation)")
            return
        }
        dirty = true
    }

    fun removeModifier(modifier: AttributeModifier) {
        ensureDataOwnership()
        modifiersById.remove(modifier.id)
        modifiersByOp[modifier.operation]?.remove(modifier.id)
        dirty = true
    }

    fun removeModifier(id: Key) {
        ensureDataOwnership()
        getModifier(id)?.let { removeModifier(it) }
        dirty = true
    }

    fun removeModifiers() {
        ensureDataOwnership()
        modifiersById.clear()
        modifiersByOp.clear()
        dirty = true
    }

    fun replace(other: AttributeInstanceDelegation) {
        this.baseValue = other.baseValue
        this.modifiersById.clear()
        this.modifiersById.putAll(other.modifiersById)
        this.modifiersByOp.clear()
        other.modifiersByOp.forEach { (op, mod) -> this.modifiersByOp[op] = Object2ObjectOpenHashMap(mod) }
        this.dirty = other.dirty
        this.cachedValue = other.cachedValue
    }

    fun copy(): AttributeInstanceDelegation {
        copyOnWrite = true
        return AttributeInstanceDelegation(
            attribute = attribute,
            baseValue = baseValue,
            modifiersById = modifiersById,
            modifiersByOp = modifiersByOp,
            cachedValue = cachedValue,
            dirty = dirty,
            copyOnWrite = true
        )
    }

    private fun ensureDataOwnership() {
        if (copyOnWrite) {
            // 深度拷贝 modifiersById
            modifiersById = Object2ObjectArrayMap(modifiersById)

            // 深度拷贝 modifiersByOp
            val modifiersByOp0 = modifiersByOp
            modifiersByOp = enumMap()
            modifiersByOp0.forEach { (op, mod) -> modifiersByOp[op] = Object2ObjectOpenHashMap(mod) }

            // 标记已经拥有所有权
            copyOnWrite = false
        }
    }

    private fun calculateValue(): Double {
        var x: Double = getBaseValue()
        getModifiersOrReturnEmpty(Operation.ADD).forEach { x += it.amount }
        var y: Double = x
        getModifiersOrReturnEmpty(Operation.MULTIPLY_BASE).forEach { y += x * it.amount }
        getModifiersOrReturnEmpty(Operation.MULTIPLY_TOTAL).forEach { y *= 1.0 + it.amount }
        return this.attribute.sanitizeValue(y)
    }

    private fun getModifiersOrReturnEmpty(operation: Operation): ObjectCollection<AttributeModifier> {
        return modifiersByOp[operation]?.values ?: ObjectSets.emptySet()
    }
}

/**
 * A prototype of [AttributeInstance].
 *
 * It's solely used to create new instances of [VanillaAttributeInstance] and
 * [WakameAttributeInstance], and should not be stored in the world state.
 */
private class ProtoAttributeInstance(
    private val delegation: AttributeInstanceDelegation,
) : AttributeInstance {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getSnapshot(): AttributeInstanceSnapshot =
        AttributeInstanceSnapshotImpl(delegation.copy())

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun setBaseValue(baseValue: Double) =
        delegation.setBaseValue(baseValue)

    override fun getModifier(id: Key): AttributeModifier? =
        delegation.getModifier(id)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun addModifier(modifier: AttributeModifier) =
        delegation.addModifier(modifier)

    override fun addTransientModifier(modifier: AttributeModifier) =
        delegation.addTransientModifier(modifier)

    override fun removeModifier(modifier: AttributeModifier) =
        delegation.removeModifier(modifier)

    override fun removeModifier(id: Key) =
        delegation.removeModifier(id)

    override fun removeModifiers() =
        delegation.removeModifiers()

    override fun replaceBaseValue(other: AttributeInstance) {
        delegation.setBaseValue(other.getBaseValue())
    }

    override fun replace(other: AttributeInstance): Unit =
        throw UnsupportedOperationException("This operation is not supported for prototype instances.")
}

/**
 * A wakame [AttributeInstance].
 *
 * This class represents the concrete attribute instance in our own system.
 */
private class WakameAttributeInstance(
    private val delegation: AttributeInstanceDelegation,
) : AttributeInstance {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getSnapshot(): AttributeInstanceSnapshot =
        AttributeInstanceSnapshotImpl(delegation.copy())

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun setBaseValue(baseValue: Double) =
        delegation.setBaseValue(baseValue)

    override fun getModifier(id: Key): AttributeModifier? =
        delegation.getModifier(id)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun addModifier(modifier: AttributeModifier) =
        delegation.addModifier(modifier)

    override fun addTransientModifier(modifier: AttributeModifier) =
        delegation.addTransientModifier(modifier)

    override fun removeModifier(modifier: AttributeModifier) =
        delegation.removeModifier(modifier)

    override fun removeModifier(id: Key) =
        delegation.removeModifier(id)

    override fun removeModifiers() =
        delegation.removeModifiers()

    override fun replaceBaseValue(other: AttributeInstance) {
        delegation.setBaseValue(other.getBaseValue())
    }

    override fun replace(other: AttributeInstance) {
        if (other is WakameAttributeInstance) {
            delegation.replace(other.delegation)
        } else {
            this.setBaseValue(other.getBaseValue())
            this.getModifiers().forEach { this.removeModifier(it) }
            other.getModifiers().forEach { this.addTransientModifier(it) }
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
    private val handle: BukkitAttributeInstance, // 封装的世界状态中的对象
    player: Player?, // 是否是玩家的属性
) : AttributeInstance {
    private val _player: WeakReference<Player> = WeakReference(player)
    private val player: Player?
        get() = _player.get()

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

    override fun getModifier(id: Key): AttributeModifier? {
        return handle.getModifier(id)?.toNeko()
    }

    override fun getModifiers(): Set<AttributeModifier> {
        return handle.modifiers.mapTo(ObjectArraySet()) { it.toNeko() } // copy
    }

    override fun hasModifier(modifier: AttributeModifier): Boolean {
        return handle.getModifier(modifier.id) != null
    }

    // 开发日记 2024/10/18
    // 选择 addModifier 还是 addTransientModifier?
    //
    // 如果玩家手持 wakame 物品, 并且 wakame 物品上有增加最大生命值的属性 (原版属性),
    // 那么当玩家先离线, 然后再上线, 后台会抛异常: “Modifier is already applied on this attribute!”
    // 这是因为玩家下线时, 并不会触发移除物品属性的逻辑, 导致属性被永久保存到 NBT 了.
    // 解决办法: addTransientModifier
    //
    // 如果不是玩家, 直接添加永久 modifier 即可.
    // 这样, 生物的原版属性将由原版来应用和持久化.

    override fun addModifier(modifier: AttributeModifier) {
        handle.addModifier(modifier.toBukkit())
    }

    override fun addTransientModifier(modifier: AttributeModifier) {
        handle.addTransientModifier(modifier.toBukkit())
    }

    override fun removeModifier(modifier: AttributeModifier) {
        handle.removeModifier(modifier.toBukkit())
    }

    override fun removeModifier(id: Key) {
        handle.removeModifier(id)
    }

    override fun removeModifiers() {
        handle.modifiers.forEach { removeModifier(it.key()) }
    }

    override fun replace(other: AttributeInstance) {
        setBaseValue(other.getBaseValue())
        this.getModifiers().forEach { removeModifier(it) }
        other.getModifiers().forEach { addTransientModifier(it) }
    }

    override fun replaceBaseValue(other: AttributeInstance) {
        setBaseValue(other.getBaseValue())
    }
}

private class ImaginaryAttributeInstanceImpl(
    private val delegation: AttributeInstanceDelegation,
) : ImaginaryAttributeInstance {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun getModifier(id: Key): AttributeModifier? =
        delegation.getModifier(id)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun getSnapshot(): AttributeInstanceSnapshot =
        AttributeInstanceSnapshotImpl(delegation.copy())
}

private class AttributeInstanceSnapshotImpl(
    private val delegation: AttributeInstanceDelegation,
) : AttributeInstanceSnapshot {
    override val attribute: Attribute
        get() = delegation.attribute

    override fun getValue(): Double =
        delegation.getValue()

    override fun getBaseValue(): Double =
        delegation.getBaseValue()

    override fun setBaseValue(baseValue: Double) =
        delegation.setBaseValue(baseValue)

    override fun getModifier(id: Key): AttributeModifier? =
        delegation.getModifier(id)

    override fun getModifiers(): Set<AttributeModifier> =
        delegation.getModifiers()

    override fun hasModifier(modifier: AttributeModifier): Boolean =
        delegation.hasModifier(modifier)

    override fun addModifier(modifier: AttributeModifier) =
        delegation.addModifier(modifier)

    override fun removeModifier(modifier: AttributeModifier) =
        delegation.removeModifier(modifier)

    override fun removeModifier(id: Key) =
        delegation.removeModifier(id)

    override fun removeModifiers() =
        delegation.removeModifiers()

    override fun toImaginary(): ImaginaryAttributeInstance =
        ImaginaryAttributeInstanceImpl(delegation.copy())
}
