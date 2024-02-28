package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.binary.core.BinaryCoreFactory
import cc.mewcraft.wakame.item.binary.core.emptyBinaryCore
import cc.mewcraft.wakame.item.binary.curse.BinaryCurseFactory
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.curse.emptySchemeCurse
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * A factory used to create [BinaryCell] from scheme and binary sources.
 */
object BinaryCellFactory {
    /**
     * Creates a [BinaryCell] from the [compoundTag].
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
     *   Compound('condition')
     *     String('id'): 'condition:entity_kills'
     *     String('index'): 'demo_bosses_1'
     *     Short('count'): 18s
     * ```
     *
     * @param compoundTag the tag
     * @return a new [BinaryCell]
     */
    fun decode(compoundTag: CompoundShadowTag): BinaryCell {
        return BinaryCellImpl(
            canReforge = compoundTag.getBoolean(NekoTags.Cell.CAN_REFORGE),
            canOverride = compoundTag.getBoolean(NekoTags.Cell.CAN_OVERRIDE),
            binaryCore = BinaryCoreFactory.decode(compoundTag.getCompound(NekoTags.Cell.CORE)), // TODO optimization: avoid creating empty compound
            binaryCurse = BinaryCurseFactory.decode(compoundTag.getCompound(NekoTags.Cell.CURSE)),
            reforgeMeta = ReforgeMetaFactory.decode(compoundTag.getCompound(NekoTags.Cell.REFORGE))
        )
    }

    /**
     * Creates a [BinaryCell] from the [context] and [schemeCell]. **Note that
     * the return value can be nullable.** A `null` value indicates that the
     * binary cell should not exist at all on the item (due to the fact that,
     * for example, nothing is drawn out from the samples **and** the property
     * [SchemeCell.keepEmpty] is configured as `false`).
     *
     * @param context the context
     * @param schemeCell the scheme cell
     * @return a new instance or `null`
     */
    fun generate(context: SchemeGenerationContext, schemeCell: SchemeCell): BinaryCell? {
        // make a core
        val schemeCore = schemeCell.coreSelector.pickSingle(context)
        val binaryCore = if (schemeCore != null) {
            // something is drawn out
            BinaryCoreFactory.generate(context, schemeCore)
        } else {
            // nothing is drawn out
            if (!schemeCell.keepEmpty) {
                // the `keepEmpty` is configured as `false`
                return null
            }
            // the `keepEmpty` is configured as `true`
            emptyBinaryCore()
        }

        // make a curse
        val schemeCurse = schemeCell.curseSelector.pickSingle(context) ?: emptySchemeCurse()
        val binaryCurse = schemeCurse.generate(context)

        // make a reforge meta (empty for new cell)
        val reforgeMeta = emptyReforgeMeta()

        val ret = BinaryCellImpl(
            canReforge = schemeCell.canReforge,
            canOverride = schemeCell.canOverride,
            binaryCore = binaryCore,
            binaryCurse = binaryCurse,
            reforgeMeta = reforgeMeta
        )
        return ret
    }
}