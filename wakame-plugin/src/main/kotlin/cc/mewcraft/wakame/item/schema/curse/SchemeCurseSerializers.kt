package cc.mewcraft.wakame.item.schema.curse

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.SchemaCursePool
import cc.mewcraft.wakame.item.schema.filter.CurseContextHolder
import cc.mewcraft.wakame.item.schema.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode

/**
 * @see AbstractGroupSerializer
 */
internal data object SchemaCurseGroupSerializer : AbstractGroupSerializer<SchemaCurse, SchemaGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): SchemaCursePool {
        return node.requireKt<SchemaCursePool>()
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
internal data object SchemaCursePoolSerializer : AbstractPoolSerializer<SchemaCurse, SchemaGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): SchemaCurse {
        return SchemaCurseFactory.schemaOf(node)
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun onPickSample(content: SchemaCurse, context: SchemaGenerationContext) {
        context.curses += CurseContextHolder(content.key)
    }
}