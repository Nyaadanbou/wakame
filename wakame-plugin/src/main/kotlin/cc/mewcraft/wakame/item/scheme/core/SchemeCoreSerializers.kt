package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.attribute.facade.element
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.cell.SchemeCorePool
import cc.mewcraft.wakame.item.scheme.filter.AbilityFilter
import cc.mewcraft.wakame.item.scheme.filter.AttributeFilter
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode

/**
 * @see AbstractGroupSerializer
 */
internal class SchemeCoreGroupSerializer : AbstractGroupSerializer<SchemeCore, SchemeGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): SchemeCorePool {
        return node.requireKt<SchemeCorePool>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
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
internal class SchemeCorePoolSerializer : AbstractPoolSerializer<SchemeCore, SchemeGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): SchemeCore {
        return SchemeCoreFactory.schemeOf(node)
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun intrinsicConditions(content: SchemeCore): Condition<SchemeGenerationContext> {
        return when (content) {
            // By design, an attribute is considered generated
            // if there is already an attribute with all the same
            // key, operation and element in the selection context.
            is SchemeAttributeCore -> AttributeFilter(true, content.key, content.data.operation, content.data.element)

            // By design, an ability is considered generated
            // if there is already an ability with the same key
            // in the selection context.
            is SchemeAbilityCore -> AbilityFilter(true, content.key)
        }
    }

    override fun onPickSample(content: SchemeCore, context: SchemeGenerationContext) {
        // context writes are delayed after the scheme is realized
    }
}
