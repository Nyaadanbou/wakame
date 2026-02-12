package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.BOOTSTRAP,
    runAfter = [
        ElementRegistryLoader::class, // deps: 反序列化时必须知道所有已知的元素类型
    ]
)
internal object AttributeSupplierRegistryLoader : RegistryLoader {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        BuiltInRegistries.ATTRIBUTE_SUPPLIER.resetRegistry()
        consumeData(BuiltInRegistries.ATTRIBUTE_SUPPLIER::add)
        BuiltInRegistries.ATTRIBUTE_SUPPLIER.freeze()
    }

    fun reload() {
        consumeData(BuiltInRegistries.ATTRIBUTE_SUPPLIER::update)
    }

    private fun consumeData(registryAction: (KoishKey, AttributeSupplier) -> Unit) {
        val loader = yamlLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText()).node("entity_attributes")
        val dataMap = AttributeSupplierSerializer.deserialize(rootNode)
        dataMap.forEach { (k, v) ->
            registryAction(k, v)
        }
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
 *       <attribute_bundle_id_1>: ~
 *       <attribute_bundle_id_2>: ~
 *       <attribute_bundle_id_3>:
 *         <element_id_1>: ~
 *         <element_id_2>: ~
 *   minecraft:mob:
 *     parent: minecraft:living
 *     values:
 *       <attribute_bundle_id_4>: ~
 *       <attribute_bundle_id_5>: ~
 * ```
 */
private object AttributeSupplierSerializer {
    /**
     * Validates the map of nodes.
     *
     * @param valuesMap
     * @return the [valuesMap]
     */
    private fun validateValuesMap(valuesMap: Map<String, ConfigurationNode>): Map<String, ConfigurationNode> {
        // This will validate two things:
        // 1. The format of the bundle id is correct
        // 2. The config node has correct structure
        for ((bundleId, valueNode) in valuesMap) {
            if (!ATTRIBUTE_ID_PATTERN_STRING.toRegex().matches(bundleId)) {
                error("The attribute bundle id '$bundleId' is in illegal format (allowed pattern: ${ATTRIBUTE_ID_PATTERN_STRING})")
            }
            if (bundleId in Attributes.elementAttributeNames && !valueNode.isMap && !valueNode.empty() && valueNode.rawScalar() == null) {
                error("The config node '$bundleId' has neither a map structure, nor a scalar value, nor a null")
            }
        }

        return valuesMap // return what it was
    }

    /**
     * Deserializes the node into a map of [AttributeSupplier].
     *
     * The assumed node structure is described below:
     *
     * ```yaml
     * <root>:
     *   minecraft:living:
     *     parent: ~
     *     values:
     *       <attribute_bundle_id_1>: ~
     *       <attribute_bundle_id_2>: ~
     *       <attribute_bundle_id_3>:
     *         <element_id_1>: ~
     *         <element_id_2>: ~
     *   minecraft:mob:
     *     parent: minecraft:living
     *     values:
     *       <attribute_bundle_id_4>: ~
     *       <attribute_bundle_id_5>: ~
     * ```
     * @return the deserialized objects
     */
    fun deserialize(rootNode: ConfigurationNode): Map<KoishKey, AttributeSupplier> {
        // The builders that have been deserialized successfully so far
        val builders = mutableMapOf<Key, AttributeSupplierBuilder>()

        // Transform the keys of the children map to ResourceLocation
        val nodeMap = rootNode.childrenMap().transformKeys<KoishKey>()

        // An extension to reduce duplicates
        fun AttributeSupplierBuilder.add(
            attribute: Attribute,
            value: Double?,
        ): AttributeSupplierBuilder {
            if (value != null) {
                add(attribute, value)
            } else {
                add(attribute)
            }
            return this
        }

        // An extension to reduce duplicates
        fun AttributeSupplierBuilder.add(
            attributes: Collection<Attribute>, // all from the bundle attribute
            valueNode: ConfigurationNode, // the node holding the value
        ): AttributeSupplierBuilder {
            for (attribute in attributes) {
                val value: Double? =
                    if (valueNode.string == "default") {
                        null // we use null to indicate default value
                    } else {
                        valueNode.get<Double>()
                    }

                this.add(attribute, value)
            }
            return this
        }

        // Creates a builder from the given data
        fun parseBuilder(
            builders: Map<KoishKey, AttributeSupplierBuilder>,
            parentKey: Key?,
            valuesMap: Map<String, ConfigurationNode>,
        ): AttributeSupplierBuilder {
            // Create the builder. Inherit the parent builder if specified
            val builder = if (parentKey != null) {
                requireNotNull(builders[parentKey]?.copy()) { "Invalid parent '$parentKey'!\n\nMake sure you have defined the parent before 'this' in the configuration!" }
            } else {
                AttributeSupplierBuilder()
            }

            // Put data into the builder
            for ((bundleId, valueNode) in valuesMap) {
                if (bundleId in Attributes.elementAttributeNames) {
                    // it's a node for elemental attributes

                    if (valueNode.isMap) {
                        // it's a map - there are possibly individual definition for each specified element

                        val valueNodeMap = valueNode.childrenMap().mapKeys { (key, _) -> key.toString() }
                        for ((elementId, valueNodeInMap) in valueNodeMap) {
                            if (!BuiltInRegistries.ELEMENT.containsId(elementId)) error("Invalid element id: '$elementId'")
                            val bundleIdWithElement = "$bundleId/${elementId.replace(':', '.')}"
                            val attributes = Attributes.getList(bundleIdWithElement)
                            builder.add(attributes, valueNodeInMap)
                        }
                    } else {
                        // not a map - then we assume it's a scalar, so
                        // the value node is used for every single element available in the system

                        for (elementType in BuiltInRegistries.ELEMENT.entrySequence) {
                            val bundleIdWithElement = "$bundleId/${elementType.getIdAsString().replace(':', '.')}"
                            val attributes = Attributes.getList(bundleIdWithElement)
                            builder.add(attributes, valueNode)
                        }
                    }

                } else {
                    // it's a node for any other attributes

                    val attributes = Attributes.getList(bundleId)
                    builder.add(attributes, valueNode)
                }
            }

            // Return the builder
            return builder
        }

        for ((entityKey, entityNode) in nodeMap) {
            val parentNode = entityNode.node("parent")
            val valuesNode = entityNode.node("values")

            val parentKey = parentNode.get<Key>()
            val valuesMap = valuesNode.childrenMap().mapKeys { (key, _) -> key.toString() }.run(::validateValuesMap)
            builders[entityKey] = parseBuilder(builders, parentKey, valuesMap)
        }

        return builders.mapValues { (_, builder) -> builder.build() }
    }
}