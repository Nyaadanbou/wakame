package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.meta.*
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.asNamespacedKey
import org.bukkit.Registry

object PaperNekoItemRealizer : NekoItemRealizer {
    override fun realize(nekoItem: NekoItem, context: SchemaGenerationContext): NekoStack {
        return createItemStack0(nekoItem, context)
    }

    override fun realize(nekoItem: NekoItem, user: User<*>): NekoStack {
        return realize0(nekoItem, user)
    }

    override fun realize(nekoItem: NekoItem, crate: Crate): NekoStack {
        return realize0(nekoItem, crate)
    }

    private fun realize0(nekoItem: NekoItem, source: Any): NekoStack {
        val context = SchemaGenerationContext(SchemaGenerationTrigger.wrap(source))
        val nekoStack = createItemStack0(nekoItem, context)
        return nekoStack
    }

    /**
     * Creates a NekoStack with the [context].
     *
     * @param nekoItem the item blueprint
     * @param context the input context
     * @return a new once-off NekoStack
     */
    private fun createItemStack0(nekoItem: NekoItem, context: SchemaGenerationContext): NekoStack {
        val nekoStack = run {
            val namespacedKey = nekoItem.material.asNamespacedKey
            val material = requireNotNull(Registry.MATERIAL.get(namespacedKey)) { "Can't find material with key '{${nekoItem.material}'" }
            NekoStackFactory.new(material)
        }

        // write base data
        nekoStack.putKey(nekoItem.key)
        nekoStack.putVariant(0)
        nekoStack.putSeed(context.seed) // TODO 对于没有随机元素的物品（例如材料类物品），不应该写入带有随机元素的数据

        // write meta
        with(nekoStack.meta) {

            // write "standalone" meta
            generateAndSet<_, SDisplayNameMeta, BDisplayNameMeta>(nekoItem, context)
            generateAndSet<_, SDisplayLoreMeta, BDisplayLoreMeta>(nekoItem, context)
            generateAndSet<_, SDurabilityMeta, BDurabilityMeta>(nekoItem, context)
            generateAndSet<_, SLevelMeta, BLevelMeta>(nekoItem, context)
            generateAndSet<_, SRarityMeta, BRarityMeta>(nekoItem, context)
            generateAndSet<_, SElementMeta, BElementMeta>(nekoItem, context)
            generateAndSet<_, SKizamiMeta, BKizamiMeta>(nekoItem, context)
            generateAndSet<_, SSkinMeta, BSkinMeta>(nekoItem, context)
            generateAndSet<_, SSkinOwnerMeta, BSkinOwnerMeta>(nekoItem, context)
        }

        // write cells
        with(nekoStack.cell) {

            // write "standalone" cells
            nekoItem.cell.forEach { (id, schema) ->
                // the order of cell population should be the same as
                // that they are declared in the configuration list

                val cell = BinaryCellFactory.generate(context, schema)
                if (cell != null) {
                    // if the binary cell is non-null, it's either:
                    // 1) a cell with some content, or
                    // 2) a cell with no content + keepEmpty is true
                    put(id, cell)
                }
                // if it's null, simply don't put the cell
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
 * @param item the item
 * @param context the context
 * @param V the type of item meta value, shared by [S] and [B]
 * @param S the type of [SchemaItemMeta]
 * @param B the type of [BinaryItemMeta]
 */
private inline fun <V, reified S : SchemaItemMeta<V>, reified B : BinaryItemMeta<V>> ItemMetaAccessor.generateAndSet(
    item: NekoItem,
    context: SchemaGenerationContext,
) {
    val meta = item.meta<S>()
    val value = meta.generate(context)
    if (value is GenerationResult.Thing) {
        getAccessorOrCreate<B>().set(value.value)
    }
}
