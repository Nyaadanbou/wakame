package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.cell.isNoop
import cc.mewcraft.wakame.item.binary.meta.*
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.asBukkit
import org.bukkit.Registry

object PaperNekoItemRealizer : NekoItemRealizer {
    override fun realize(item: NekoItem, context: SchemaGenerationContext): NekoStack {
        return createItemStack0(item, context)
    }

    override fun realize(item: NekoItem, user: User<*>): NekoStack {
        return realize0(item, user)
    }

    override fun realize(item: NekoItem, crate: Crate): NekoStack {
        return realize0(item, crate)
    }

    private fun realize0(item: NekoItem, source: Any): NekoStack {
        val context = SchemaGenerationContext(SchemaGenerationTrigger.wrap(source))
        val nekoStack = createItemStack0(item, context)
        return nekoStack
    }

    /**
     * Creates a NekoStack with the [context].
     *
     * @param item the item blueprint
     * @param context the input context
     * @return a new once-off NekoStack
     */
    private fun createItemStack0(item: NekoItem, context: SchemaGenerationContext): NekoStack {
        val nekoStack = run {
            val key = item.material.asBukkit
            val mat = requireNotNull(Registry.MATERIAL.get(key)) { "Can't find material by key '${item.material}'" }
            PlayNekoStackFactory.new(mat)
        }

        //
        // Write base data
        //
        with(nekoStack) {
            putKey(item.key)
            putVariant(0)
        }

        //
        // Write item meta
        //
        with(nekoStack.meta) {
            // Caution: the order of generation matters here!

            generateMeta<_, SDisplayNameMeta, BDisplayNameMeta>(item, context)
            generateMeta<_, SDisplayLoreMeta, BDisplayLoreMeta>(item, context)
            generateMeta<_, SDurabilityMeta, BDurabilityMeta>(item, context)
            generateMeta<_, SLevelMeta, BLevelMeta>(item, context)
            generateMeta<_, SRarityMeta, BRarityMeta>(item, context)
            generateMeta<_, SElementMeta, BElementMeta>(item, context)
            generateMeta<_, SKizamiMeta, BKizamiMeta>(item, context)
            generateMeta<_, SSkinMeta, BSkinMeta>(item, context)
            generateMeta<_, SSkinOwnerMeta, BSkinOwnerMeta>(item, context)
        }

        //
        // Write item cell
        //
        with(nekoStack.cell) {
            item.cellMap.forEach { (id, schemaCell) ->
                // the order of cell population should be the same as
                // that they are declared in the configuration list

                val binaryCell = BinaryCellFactory.reify(schemaCell, context)
                if (!binaryCell.isNoop) {
                    // we only write cell if it's not a noop
                    put(id, binaryCell)
                }
            }
        }

        return nekoStack
    }
}

/**
 * Generates meta from [item] and [context] and then writes it to [ItemMetaAccessor].
 *
 * **Only if something is generated will the item meta be written out.**
 *
 * @param item the neko item
 * @param context the generation context
 * @param V the type of item meta value, shared by [S] and [B]
 * @param S the type of [SchemaItemMeta]
 * @param B the type of [BinaryItemMeta]
 */
private inline fun <V, reified S : SchemaItemMeta<V>, reified B : BinaryItemMeta<V>> ItemMetaAccessor.generateMeta(
    item: NekoItem,
    context: SchemaGenerationContext,
) {
    val meta = item.getMeta<S>()
    val value = meta.generate(context)
    if (value is GenerationResult.Thing) {
        getAccessor<B>().set(value.value)
    }
}
