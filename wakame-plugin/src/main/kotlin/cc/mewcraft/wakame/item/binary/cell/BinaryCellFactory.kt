package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCoreFactory
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurseFactory
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurseFactory
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * A factory used to create [BinaryCell] from schema and binary sources.
 */
object BinaryCellFactory {
    /**
     * Creates a [BinaryCell] from a NBT source.
     *
     * The [compoundTag] structure is like this:
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
     * @param compoundTag the tag
     * @return a new [BinaryCell]
     */
    fun decode(compoundTag: CompoundShadowTag): BinaryCell {
        return ImmutableBinaryCell(
            canReforge = compoundTag.getBoolean(NekoTags.Cell.CAN_REFORGE),
            canOverride = compoundTag.getBoolean(NekoTags.Cell.CAN_OVERRIDE),
            binaryCore = BinaryCoreFactory.decode(compoundTag.getCompound(NekoTags.Cell.CORE)),
            binaryCurse = BinaryCurseFactory.decode(compoundTag.getCompound(NekoTags.Cell.CURSE)),
            reforgeMeta = ReforgeMetaFactory.decode(compoundTag.getCompound(NekoTags.Cell.REFORGE))
        )
    }

    /**
     * Creates a [BinaryCell] from a schema source.
     *
     * **Note that the return value can be nullable.** A `null` value indicates
     * that the binary cell should not exist at all on the item (due to the fact that,
     * for example, nothing is drawn out from the samples **and** the property
     * [SchemaCell.keepEmpty] is configured as `false`).
     *
     * @param context the context
     * @param schemaCell the schema cell
     * @return a new instance or `null`
     */
    fun generate(context: SchemaGenerationContext, schemaCell: SchemaCell): BinaryCell? {
        // make a core
        val binaryCore = run {
            val schemaCore = schemaCell.coreSelector.pickSingle(context)
            if (schemaCore != null) {
                // something is drawn out
                BinaryCoreFactory.generate(context, schemaCore)
            } else {
                // nothing is drawn out
                if (!schemaCell.keepEmpty) {
                    // the `keepEmpty` is configured as `false`
                    return null
                }
                // the `keepEmpty` is configured as `true`
                BinaryCoreFactory.empty()
            }
        }

        // make a curse
        val binaryCurse = run {
            val schema = schemaCell.curseSelector.pickSingle(context) ?: SchemaCurseFactory.empty()
            val binary = BinaryCurseFactory.generate(context, schema)
            binary
        }

        // make a reforge meta
        val reforgeMeta = ReforgeMetaFactory.empty()

        // collect all and return
        return ImmutableBinaryCell(
            canReforge = schemaCell.canReforge,
            canOverride = schemaCell.canOverride,
            binaryCore = binaryCore,
            binaryCurse = binaryCurse,
            reforgeMeta = reforgeMeta
        )
    }
}