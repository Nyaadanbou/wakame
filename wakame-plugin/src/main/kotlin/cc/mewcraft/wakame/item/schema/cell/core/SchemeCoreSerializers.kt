package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.SchemaCorePool
import cc.mewcraft.wakame.item.schema.cell.core.attribute.SchemaAttributeCore
import cc.mewcraft.wakame.item.schema.cell.core.attribute.elementOrNull
import cc.mewcraft.wakame.item.schema.cell.core.empty.SchemaEmptyCore
import cc.mewcraft.wakame.item.schema.cell.core.noop.SchemaNoopCore
import cc.mewcraft.wakame.item.schema.cell.core.skill.SchemaSkillCore
import cc.mewcraft.wakame.item.schema.filter.AttributeFilter
import cc.mewcraft.wakame.item.schema.filter.FilterFactory
import cc.mewcraft.wakame.item.schema.filter.SkillFilter
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException

/**
 * @see AbstractGroupSerializer
 */
internal data object SchemaCoreGroupSerializer : AbstractGroupSerializer<SchemaCore, SchemaGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): SchemaCorePool {
        return node.krequire<SchemaCorePool>()
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
 *   - key: attribute:movement_speed
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
 *     - key: attribute:movement_speed
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
        return SchemaCoreFactory.create(node)
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun intrinsicConditions(content: SchemaCore): Condition<SchemaGenerationContext> {
        return when (content) {
            // A noop core should always return true
            is SchemaNoopCore -> Condition.alwaysTrue()

            // An empty core should always return true
            is SchemaEmptyCore -> Condition.alwaysTrue()

            // By design, an attribute is considered generated
            // if there is already an attribute with all the same
            // key, operation and element in the selection context.
            is SchemaAttributeCore -> AttributeFilter(true, content.key, content.operation, content.elementOrNull)

            // By design, a skill is considered generated
            // if there is already a skill with the same key
            // in the selection context, neglecting the trigger.
            is SchemaSkillCore -> SkillFilter(true, content.key)

            // Throw if we see an unknown schema core type
            else -> throw SerializationException("Can't create intrinsic conditions for unknown schema core type: ${content::class.qualifiedName}")
        }
    }

    override fun onPickSample(content: SchemaCore, context: SchemaGenerationContext) {
        // context writes are delayed after the schema is realized
    }
}
