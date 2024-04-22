package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataFactory
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.cell.core.noop.SchemaNoopCore
import cc.mewcraft.wakame.item.schema.cell.curse.type.SchemaEmptyCurse
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * A factory used to create [BinaryCell] from schema and binary sources.
 */
object BinaryCellFactory {
    /**
     * Wraps a NBT as [BinaryCell].
     *
     * The [compound] structure is as following:
     *
     * ```
     * Compound('...')
     *   Boolean('can_reforge'): true
     *   Boolean('can_override'): false
     *   Compound('core')
     *     String('id'): 'attribute:attack_damage'
     *     Short('min'): 10s
     *     Short('max'): 15s
     *     Byte('elem'): 0b
     *     Byte('op'): 0b
     *   Compound('reforge')
     *     Byte('success'): 5b
     *     Byte('failure'): 1b
     *   Compound('curse')
     *     String('id'): 'condition:entity_kills'
     *     String('index'): 'demo_bosses_1'
     *     Short('count'): 18s
     * ```
     *
     * @param compound the compound tag containing the cell
     * @return a new [BinaryCell]
     */
    fun wrap(compound: CompoundShadowTag): BinaryCell {
        return BinaryCellNBTWrapper(compound)
    }

    /**
     * Reifies a [SchemaCell].
     *
     * **Note that the return value can be nullable.** A `null` value indicates
     * that the binary cell should not exist on the item at all.
     *
     * @param context the generation context
     * @param schema the schema cell
     * @return a new instance or `null`
     */
    fun reify(schema: SchemaCell, context: SchemaGenerationContext): BinaryCell {
        // make a core
        val core = run {
            val schemaCore = schema.createOptions.core.pickSingle(context) ?: SchemaNoopCore()
            val binaryCore = schemaCore.reify(context)
            binaryCore
        }

        // make a curse
        val curse = run {
            val schemaCurse = schema.createOptions.curse.pickSingle(context) ?: SchemaEmptyCurse()
            val binaryCurse = schemaCurse.reify(context)
            binaryCurse
        }

        // make a reforge meta
        val reforgeData = ReforgeDataFactory.empty()

        // collect all and return
        return BinaryCellDataHolder(core, curse, reforgeData)
    }
}