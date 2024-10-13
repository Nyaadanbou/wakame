package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.Key
import com.google.common.collect.ImmutableMap
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attributable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

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
        product.replace(prototype) // 将实例的值替换为原型的值
        return product
    }

    /**
     * Creates a new [ImaginaryAttributeInstance] from this supplier.
     */
    fun createImaginaryInstance(type: Attribute): ImaginaryAttributeInstance? {
        val prototype = prototypes[type] ?: return null
        val product = AttributeInstanceFactory.createDataInstance(type)
        product.replace(prototype)
        val snapshot = product.getSnapshot() // 创建快照
        return snapshot.toIntangible() // 转为不可变
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
            "Can't find attribute modifier '$id' on attribute '${type.descriptionId}'"
        }
    }

    fun getModifierValue(type: Attribute, id: Key): Double {
        return requireNotNull(getDefault(type).getModifier(id)?.amount) {
            "Can't find attribute modifier '$id' on attribute '${type.descriptionId}'"
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
            requireNotNull(prototypes[type]) {
                val id = type.descriptionId
                val element = (type as? ElementAttribute)?.element?.uniqueId
                "Can't find attribute instance for attribute '$id ($element)'"
            }
        }
    }

    /**
     * 获取指定的原型.
     *
     * @throws IllegalArgumentException 如果 [type] 不在此供应者中
     */
    private fun getDefault(type: Attribute): AttributeInstance {
        return requireNotNull(prototypes[type]) {
            val id = type.descriptionId
            val element = (type as? ElementAttribute)?.element?.uniqueId
            "Can't find attribute instance for attribute '$id ($element)'"
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
        private fun AttributeSupplierBuilder.add(
            attribute: Attribute, value: Double?,
        ): AttributeSupplierBuilder {
            if (value != null) {
                add(attribute, value)
            } else {
                add(attribute)
            }
            return this
        }

        // Just an extension to reduce duplicates
        private fun AttributeSupplierBuilder.add(
            /* all from the same facade */ attributes: Collection<Attribute>,
            /* the node holding the value */ valueNode: ConfigurationNode,
        ): AttributeSupplierBuilder {
            for (attribute in attributes) {
                val value: Double? = run {
                    if (valueNode.string == "default") {
                        null // we use null to indicate default value
                    } else {
                        valueNode.get<Double>()
                    }
                }

                this.add(attribute, value)
            }
            return this
        }

        fun build(): AttributeSupplierBuilder {
            // Create the builder. Inherit the parent builder if specified
            val builder = if (parentKey != null) {
                requireNotNull(
                    builders[parentKey]?.copy()
                ) {
                    "Can't find parent '$parentKey'. Make sure you have defined the parent before \"this\" in the configuration!"
                }
            } else {
                AttributeSupplierBuilder()
            }

            // Put data into the builder
            for ((facadeId, valueNode) in valuesMap) {
                if (facadeId in Attributes.getElementAttributeNames()) {
                    // it's a node for elemental attributes

                    if (valueNode.isMap) {
                        // it's a map - there are possibly individual definition for each specified element

                        val valueNodeMap = valueNode.childrenMap().mapKeys { (key, _) -> key.toString() }
                        for ((elementId, valueNodeInMap) in valueNodeMap) {
                            val attributes = Attributes.element(ElementRegistry.INSTANCES[elementId]).getCollectionBy(facadeId)
                            builder.add(attributes, valueNodeInMap)
                        }
                    } else {
                        // not a map - then we assume it's a scalar -
                        // the value node is used for every single element available in the system

                        for ((_, elementType) in ElementRegistry.INSTANCES) {
                            val attributes = Attributes.element(elementType).getCollectionBy(facadeId)
                            builder.add(attributes, valueNode)
                        }
                    }

                } else {
                    // it's a node for any other attributes

                    val attributes = Attributes.getCollectionBy(facadeId)
                    builder.add(attributes, valueNode)
                }
            }

            // Return the builder
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
        // 1. The format of the facade id is correct
        // 2. The config node has correct structure
        for ((facadeId, valueNode) in valuesMap) {
            if (!AttributeSupport.ATTRIBUTE_ID_PATTERN_STRING.toRegex().matches(facadeId)) {
                error("The facade ID '$facadeId' is in illegal format (allowed pattern: ${AttributeSupport.ATTRIBUTE_ID_PATTERN_STRING})")
            }
            if (facadeId in Attributes.getElementAttributeNames() && !valueNode.isMap && !valueNode.empty() && valueNode.rawScalar() == null) {
                error("The attribute '$facadeId' has neither a map structure, nor a scalar value, nor a null")
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
            throw IllegalStateException("The function deserialize() can be invoked at most once for the instance")
        }

        val nodeMap = this.node.childrenMap().mapKeys { (key, _) -> Key(key.toString()) }

        for ((entityKey, entityNode) in nodeMap) {
            val parentNode = entityNode.node("parent")
            val valuesNode = entityNode.node("values")

            val parentKey = parentNode.get<Key>()
            val valuesMap = valuesNode.childrenMap().mapKeys { (key, _) -> key.toString() }.run(::validateValuesMap)
            this.builders[entityKey] = IntermediateBuilder(parentKey, valuesMap).build()
        }

        return this.builders
            .mapValues { (_, builder) -> builder.build() }
            .also { isFrozen = true }
    }
}