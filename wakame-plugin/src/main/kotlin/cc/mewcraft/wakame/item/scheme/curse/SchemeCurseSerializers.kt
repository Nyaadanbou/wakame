package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.cell.SchemeCursePool
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.util.typedRequire
import org.spongepowered.configurate.ConfigurationNode

internal class SchemeCurseGroupSerializer : AbstractGroupSerializer<SchemeCurse, SchemeGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): SchemeCursePool {
        return node.typedRequire<SchemeCursePool>()
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
 *    - key: curse:highest_damage
 *      weight: 1
 *      element: fire
 *      amount: 10
 *    - key: curse:highest_damage
 *      weight: 1
 *      element: water
 *      amount: 16
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: rarity
 *       rarity: common
 *   entries:
 *     - key: curse:highest_damage
 *       weight: 1
 *       element: fire
 *       amount: 20
 *     - key: curse:highest_damage
 *       weight: 1
 *       element: water
 *       amount: 32
 * ```
 */
internal class SchemeCursePoolSerializer : AbstractPoolSerializer<SchemeCurse, SchemeGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): SchemeCurse {
        return SchemeCurseFactory.schemeOf(node)
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun traceApply(content: SchemeCurse, context: SchemeGenerationContext) {
        context.curseKeys += content.key
    }
}