package cc.mewcraft.wakame.item.schema.core

import cc.mewcraft.wakame.attribute.facade.element
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.SchemaCorePool
import cc.mewcraft.wakame.item.schema.filter.AbilityFilter
import cc.mewcraft.wakame.item.schema.filter.AttributeFilter
import cc.mewcraft.wakame.item.schema.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode

/**
 * @see AbstractGroupSerializer
 */
internal data object SchemaCoreGroupSerializer : AbstractGroupSerializer<SchemaCore, SchemaGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): SchemaCorePool {
        return node.requireKt<SchemaCorePool>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }
}

/**
 * ## Node structure 1 (fallback)
 *
 * ```yaml
 * <node>:
 *   - key: attribute:attack_speed_level
 *     weight: 1
 *     value: 4
 *   - key: attribute:movement_speed_rate
 *     weight: 1
 *     value: 0.4
 *   - key: attribute:defense
 *     weight: 1
 *     value: 23
 *     meta: x1
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: rarity
 *       rarity: rare
 *   entries:
 *     - key: attribute:attack_speed_level
 *       weight: 1
 *       value: 4
 *     - key: attribute:movement_speed_rate
 *       weight: 1
 *       value: 0.4
 *     - key: attribute:defense
 *       weight: 1
 *       value: 23
 *       meta: x1
 * ```
 */
internal data object SchemaCorePoolSerializer : AbstractPoolSerializer<SchemaCore, SchemaGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): SchemaCore {
        return SchemaCoreFactory.schemaOf(node)
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun intrinsicConditions(content: SchemaCore): Condition<SchemaGenerationContext> {
        return when (content) {
            // By design, an attribute is considered generated
            // if there is already an attribute with all the same
            // key, operation and element in the selection context.
            is SchemaAttributeCore -> AttributeFilter(true, content.key, content.data.operation, content.data.element)

            // By design, an ability is considered generated
            // if there is already an ability with the same key
            // in the selection context.
            is SchemaAbilityCore -> AbilityFilter(true, content.key)
        }
    }

    override fun onPickSample(content: SchemaCore, context: SchemaGenerationContext) {
        // context writes are delayed after the schema is realized
    }
}
