package cc.mewcraft.wakame.entity.attribute

import com.google.common.collect.ImmutableMap
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attributable

/**
 * Responsible to provide default attribute values for a certain [Attributable].
 *
 * It may **not** always provide default values for every attribute type!
 *
 * @property prototypes The available [AttributeInstance]s in this supplier.
 * @property attributes The available [Attribute]s in this supplier.
 */
class AttributeSupplier
internal constructor(
    private val prototypes: Map<Attribute, AttributeInstance>,
) {
    val attributes: Set<Attribute> = prototypes.keys

    /**
     * Creates a new live [AttributeInstance] from this supplier.
     *
     * Returns `null` if the [type] is not supported by this supplier.
     *
     * This function may have **side effect** to the [attributable].

     * @param type the attribute type
     * @param attributable the object which holds the vanilla attribute instances in the world state
     * @return a new instance of [AttributeInstance]
     */
    fun createLiveInstance(type: Attribute, attributable: Attributable): AttributeInstance? {
        if (isAbsoluteVanilla(type)) {
            // 如果指定的属性是 absolute-vanilla，那么
            // - 该函数应该直接采用原版属性的值, 而非返回空值
            // - 该函数不能覆盖原版属性的任何值, 应该仅作为原版属性的代理
            return AttributeInstanceFactory.createLiveInstance(type, attributable, false)
        }

        val prototype = prototypes[type] ?: return null
        val product = AttributeInstanceFactory.createLiveInstance(type, attributable, true)
        product.replaceBaseValue(prototype) // 将实例的值替换为原型的值
        return product
    }

    /**
     * Creates a new [ImaginaryAttributeInstance] from this supplier.
     */
    fun createImaginaryInstance(type: Attribute): ImaginaryAttributeInstance? {
        val prototype = prototypes[type] ?: return null
        val product = AttributeInstanceFactory.createDataInstance(type)
        product.replaceBaseValue(prototype)
        val snapshot = product.getSnapshot() // 创建快照
        return snapshot.toImaginary() // 转为不可变
    }

    /**
     * Gets the value for the [type] after all modifiers have been applied.
     *
     * @param type the attribute type
     * @param attributable
     * @return the value for the attribute
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getValue(type: Attribute, attributable: Attributable): Double {
        return getDefault(type, attributable).getValue()
    }

    fun getValue(type: Attribute): Double {
        return getDefault(type).getValue()
    }

    /**
     * Gets the base value for the [type].
     *
     * @param type the attribute type
     * @param attributable
     * @return the base value for the attribute
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getBaseValue(type: Attribute, attributable: Attributable): Double {
        return getDefault(type, attributable).getBaseValue()
    }

    fun getBaseValue(type: Attribute): Double {
        return getDefault(type).getBaseValue()
    }

    /**
     * Gets the modifier value specified by [type] and [id].
     *
     * @param type the attribute type
     * @param id the modifier id
     * @param attributable
     * @return the modifier value
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getModifierValue(type: Attribute, id: Key, attributable: Attributable): Double {
        return requireNotNull(getDefault(type, attributable).getModifier(id)?.amount) {
            "can't find attribute modifier '$id' on attribute '${type.id}'"
        }
    }

    fun getModifierValue(type: Attribute, id: Key): Double {
        return requireNotNull(getDefault(type).getModifier(id)?.amount) {
            "can't find attribute modifier '$id' on attribute '${type.id}'"
        }
    }

    /**
     * Checks whether this supplier has the [type].
     *
     * @param type the attribute type
     * @return `true` if this supplier has the [type]
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun hasAttribute(type: Attribute): Boolean {
        return prototypes.containsKey(type)
    }

    /**
     * Checks whether this supplier has the modifier specified by [type] and [id].
     *
     * @param type the attribute type
     * @param id the modifier id
     * @return `true` if this supplier has the modifier
     */
    fun hasModifier(type: Attribute, id: Key): Boolean {
        return prototypes[type]?.getModifier(id) != null
    }

    /**
     * Gets the prototype.
     *
     * @param type the attribute type
     * @param attributable the attributable object in the world state
     * @return the prototype
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    private fun getDefault(type: Attribute, attributable: Attributable): AttributeInstance {
        return if (isAbsoluteVanilla(type)) {
            AttributeInstanceFactory.createLiveInstance(type, attributable, false)
        } else {
            requireNotNull(prototypes[type]) { "invalid attribute instance for attribute '${type.id}'" }
        }
    }

    /**
     * 获取指定的原型.
     *
     * @throws IllegalArgumentException 如果 [type] 不在此供应者中
     */
    private fun getDefault(type: Attribute): AttributeInstance {
        return requireNotNull(prototypes[type]) { "invalid attribute instance for attribute '${type.id}'" }
    }

    /**
     * Checks whether the [type] is **absolute-vanilla**.
     *
     * We say the [type] is **absolute-vanilla** if the [Attribute.vanilla]
     * is `true` and this supplier does not have corresponding prototype of the
     * [type], from which it follows that the [AttributeInstance] for the [type]
     * should only be a proxy to the instance in the world state, and should not
     * have any side effects to the world state instance unless you explicitly
     * do so.
     *
     * @param type the attribute type
     * @return `true` if the [type] is absolute-vanilla
     */
    private fun isAbsoluteVanilla(type: Attribute): Boolean {
        return type.vanilla && !prototypes.containsKey(type)
    }
}

/**
 * The builder of [AttributeSupplier].
 */
class AttributeSupplierBuilder(
    /**
     * The prototypes in this builder.
     */
    private val prototypes: ImmutableMap.Builder<Attribute, AttributeInstance> = ImmutableMap.builder(),
) {
    /**
     * Creates a prototype instance and add it to the [prototypes] map.
     *
     * @param attribute the attribute type
     * @return the created prototype
     */
    private fun createPrototype(attribute: Attribute): AttributeInstance {
        val prototype = AttributeInstanceFactory.createPrototype(attribute)
        prototypes.put(attribute, prototype)
        return prototype
    }

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于 [attribute]。
     */
    fun add(attribute: Attribute): AttributeSupplierBuilder {
        createPrototype(attribute)
        return this
    }

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于给定的常量。
     */
    fun add(attribute: Attribute, value: Double): AttributeSupplierBuilder {
        val prototype = createPrototype(attribute)
        prototype.setBaseValue(value)
        return this
    }

    /**
     * Gets a copy of this builder.
     */
    fun copy(): AttributeSupplierBuilder {
        return AttributeSupplierBuilder(ImmutableMap.builder<Attribute, AttributeInstance>().putAll(prototypes.buildKeepingLast()))
    }

    /**
     * Builds a new [AttributeSupplier].
     */
    fun build(): AttributeSupplier {
        return AttributeSupplier(prototypes.buildKeepingLast())
    }
}

