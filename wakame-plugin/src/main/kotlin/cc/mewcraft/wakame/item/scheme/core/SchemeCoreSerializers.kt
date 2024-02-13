package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.cell.SchemeCorePool
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode

/**
 * 栏位内容组的序列化实现。
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

    override fun traceApply(content: SchemeCore, context: SchemeGenerationContext) {
        context.coreKeys += content.key
    }
}
