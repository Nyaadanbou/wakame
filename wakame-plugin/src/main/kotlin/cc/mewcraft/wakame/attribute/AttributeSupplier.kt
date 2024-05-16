package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.Key
import com.google.common.collect.ImmutableMap
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attributable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.UUID

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
    val attributes: Collection<Attribute> = prototypes.keys

    /**
     * Creates a new [AttributeInstance] from this supplier.
     *
     * Returns `null` if the [type] is not supported by this supplier.
     *
     * This function may have **side effect** to the [attributable].

     * @param type the attribute type
     * @param attributable the object which holds the vanilla attribute instances in the world state
     * @return a new instance of [AttributeInstance].
     */
    fun createInstance(type: Attribute, attributable: Attributable): AttributeInstance? {
        if (isAbsoluteVanilla(type)) {
            // 如果指定的属性是 absolute-vanilla，那么
            //  - 该函数应该直接采用原版属性的值，而非返回空值
            //  - 该函数不能覆盖原版属性的任何值，应该仅作为原版属性的代理
            return AttributeInstanceFactory.createInstance(type, attributable, false)
        }

        val prototype = prototypes[type] ?: return null
        val product = AttributeInstanceFactory.createInstance(type, attributable, true)
        product.replace(prototype) // 将实例的值替换为原型的值
        return product
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

    /**
     * Gets the modifier value specified by [type] and [uuid].
     *
     * @param type the attribute type
     * @param uuid the uuid
     * @param attributable
     * @return the modifier value
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getModifierValue(type: Attribute, uuid: UUID, attributable: Attributable): Double {
        return requireNotNull(getDefault(type, attributable).getModifier(uuid)?.amount) {
            "Can't find attribute modifier '$uuid' on attribute '${type.descriptionId}'"
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
     * Checks whether this supplier has the modifier specified by [type] and [uuid].
     *
     * @param type the attribute type
     * @param uuid the uuid
     * @return `true` if this supplier has the modifier
     */
    fun hasModifier(type: Attribute, uuid: UUID): Boolean {
        return prototypes[type]?.getModifier(uuid) != null
    }

    /**
     * Gets the prototype.
     *
     * @param type the attribute type
     * @param attributable the attributable object in the world state
     * @return the prototype
     */
    private fun getDefault(type: Attribute, attributable: Attributable): AttributeInstance {
        return if (isAbsoluteVanilla(type)) {
            AttributeInstanceFactory.createInstance(type, attributable, false) // TODO 优化这部分，避免频繁的新对象开销
        } else {
            requireNotNull(prototypes[type]) { "Can't find attribute instance for attribute '${type.descriptionId}'" }
        }
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
    private fun createInstance(attribute: Attribute): AttributeInstance {
        val prototype = AttributeInstanceFactory.createPrototype(attribute)
        prototypes.put(attribute, prototype)
        return prototype
    }

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于 [attribute]。
     */
    fun add(attribute: Attribute): AttributeSupplierBuilder {
        createInstance(attribute)
        return this
    }

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于给定的常量。
     */
    fun add(attribute: Attribute, value: Double): AttributeSupplierBuilder {
        val prototype = createInstance(attribute)
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

/**
 * The deserializer for a **collection** of [AttributeSupplier]s in the config.
 *
 * The assumed config structure is described below.
 *
 * ## Configuration Structure
 *
 * ```yaml
 * <root>:
 *   minecraft:living:
 *     parent: ~
 *     values:
 *       <attribute_facade_id_1>: ~
 *       <attribute_facade_id_2>: ~
 *       <attribute_facade_id_3>:
 *         <element_id_1>: ~
 *         <element_id_2>: ~
 *   minecraft:mob:
 *     parent: minecraft:living
 *     values:
 *       <attribute_facade_id_4>: ~
 *       <attribute_facade_id_5>: ~
 * ```
 */
internal class AttributeSupplierDeserializer(
    /**
     * The configuration node.
     */
    private val node: ConfigurationNode,
) {
    /**
     * The builders that have been deserialized successfully so far.
     */
    private val builders: MutableMap<Key, AttributeSupplierBuilder> = mutableMapOf()

    /**
     * Whether the deserializer is `frozen`. This flag is initially `false`.
     *
     * Once the function [deserialize] is invoked and returns successfully, this
     * property should be set to `true`. Invocation on [deserialize] will throw
     * [IllegalStateException] if [isFrozen] is `true`.
     */
    private var isFrozen: Boolean = false

    /**
     * Contains the intermediate logic to create a valid [AttributeSupplierBuilder].
     */
    private inner class IntermediateBuilder(
        /**
         * The parent key. TBD.
         */
        private val parentKey: Key?,
        /**
         * The values map. TBD.
         */
        private val valuesMap: Map<String, ConfigurationNode>,
    ) {
        // Just an extension to reduce duplicates
        private fun AttributeSupplierBuilder.add(attribute: Attribute, value: Double?): AttributeSupplierBuilder {
            if (value != null) {
                add(attribute, value)
            } else {
                add(attribute)
            }
            return this
        }

        fun build(): AttributeSupplierBuilder {
            // TODO add support for MythicMobs entities

            val builder = if (parentKey != null) {
                requireNotNull(builders[parentKey]?.copy()) {
                    "Can't find parent '$parentKey'. Make sure you have defined the parent before \"this\" in the configuration!"
                }
            } else {
                AttributeSupplierBuilder()
            }

            for ((facadeId, valueNode) in valuesMap) {
                if (facadeId in Attributes.ELEMENT_ATTRIBUTE_NAMES) {
                    // it's attribute types with elements

                    if (valueNode.isMap) {
                        // it's a map - there are possibly individual definitions for each element

                        val valueMap = valueNode.childrenMap()
                            .mapKeys { (key, _) -> key.toString() }
                            .mapValues { (_, value) -> value.get<Double>() /* this will give us a `Double?` */ }

                        for ((elementId, doubleValue) in valueMap) {
                            val elementType = ElementRegistry.INSTANCES[elementId]
                            val elementAttributeHolder = Attributes.byElement(elementType)
                            for (elementAttributeType in elementAttributeHolder.byFacade(facadeId)) {
                                builder.add(elementAttributeType, doubleValue)
                            }
                        }
                    } else {
                        // not a map - then we assume it's a scalar

                        val doubleValue = valueNode.get<Double>()
                        for ((_, elementType) in ElementRegistry.INSTANCES) {
                            val elementAttributeHolder = Attributes.byElement(elementType)
                            for (elementAttributeType in elementAttributeHolder.byFacade(facadeId)) {
                                builder.add(elementAttributeType, doubleValue)
                            }
                        }
                    }

                } else {
                    // any other types of attributes

                    val attributeTypes = Attributes.byFacade(facadeId)
                    val doubleValue = valueNode.get<Double>()
                    for (attributeType in attributeTypes) {
                        builder.add(attributeType, doubleValue)
                    }
                }
            }

            return builder
        }
    }

    /**
     * Validates the map of nodes.
     *
     * @param valuesMap
     * @return the [valuesMap]
     */
    private fun validateValuesMap(valuesMap: Map<String, ConfigurationNode>): Map<String, ConfigurationNode> {
        // This will validate two things:
        // 1. The facade ID is in the legal format
        // 2. The ConfigNode has correct structure
        for ((facadeId, valueNode) in valuesMap) {
            if (!AttributeSupport.ATTRIBUTE_ID_PATTERN_STRING.toRegex().matches(facadeId)) {
                error("The facade ID '$facadeId' is in illegal format (allowed pattern: ${AttributeSupport.ATTRIBUTE_ID_PATTERN_STRING})")
            }
            if (facadeId in Attributes.ELEMENT_ATTRIBUTE_NAMES && !valueNode.isMap) {
                error("The attribute '$facadeId' does not have a map structure")
            }
        }

        return valuesMap // return what it was
    }

    /**
     * Deserializes the node into a map of [AttributeSupplier].
     *
     * @return the deserialized objects
     * @throws IllegalStateException if [isFrozen] is `true`
     */
    fun deserialize(): Map<Key, AttributeSupplier> {
        if (isFrozen) {
            throw IllegalStateException("The function deserialize() can be invoked only once for a deserializer")
        }

        val nodeMap = this.node.childrenMap().mapKeys { (key, _) -> Key(key.toString()) }

        for ((entityKey, entityNode) in nodeMap) {
            val parentNode = entityNode.node("parent")
            val valuesNode = entityNode.node("values")

            val parentKey = parentNode.get<Key>()
            val valuesMap = valuesNode.childrenMap().mapKeys { (key, _) -> key.toString() }.run(::validateValuesMap)
            this.builders[entityKey] = IntermediateBuilder(
                parentKey, valuesMap
            ).build()
        }

        return this.builders
            .mapValues { (_, builder) -> builder.build() }
            .also { isFrozen = true }
    }
}