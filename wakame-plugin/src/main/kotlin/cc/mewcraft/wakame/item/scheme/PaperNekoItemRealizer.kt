package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.meta.*
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.player.Player
import cc.mewcraft.wakame.util.asNamespacedKey
import org.bukkit.Registry
import cc.mewcraft.wakame.item.binary.meta.DisplayLoreMeta as BDisplayLoreMeta
import cc.mewcraft.wakame.item.binary.meta.DisplayNameMeta as BDisplayNameMeta
import cc.mewcraft.wakame.item.binary.meta.DurabilityMeta as BDurabilityMeta
import cc.mewcraft.wakame.item.binary.meta.ElementMeta as BElementMeta
import cc.mewcraft.wakame.item.binary.meta.KizamiMeta as BKizamiMeta
import cc.mewcraft.wakame.item.binary.meta.LevelMeta as BLevelMeta
import cc.mewcraft.wakame.item.binary.meta.RarityMeta as BRarityMeta
import cc.mewcraft.wakame.item.binary.meta.SkinMeta as BSkinMeta
import cc.mewcraft.wakame.item.binary.meta.SkinOwnerMeta as BSkinOwnerMeta
import cc.mewcraft.wakame.item.scheme.meta.DisplayLoreMeta as SDisplayLoreMeta
import cc.mewcraft.wakame.item.scheme.meta.DisplayNameMeta as SDisplayNameMeta
import cc.mewcraft.wakame.item.scheme.meta.DurabilityMeta as SDurabilityMeta
import cc.mewcraft.wakame.item.scheme.meta.ElementMeta as SElementMeta
import cc.mewcraft.wakame.item.scheme.meta.KizamiMeta as SKizamiMeta
import cc.mewcraft.wakame.item.scheme.meta.LevelMeta as SLevelMeta
import cc.mewcraft.wakame.item.scheme.meta.RarityMeta as SRarityMeta
import cc.mewcraft.wakame.item.scheme.meta.SkinMeta as SSkinMeta
import cc.mewcraft.wakame.item.scheme.meta.SkinOwnerMeta as SSkinOwnerMeta

object PaperNekoItemRealizer : NekoItemRealizer {

    override fun realize(nekoItem: NekoItem, player: Player): NekoItemStack = realize0(nekoItem, player)
    override fun realize(nekoItem: NekoItem, crate: Crate): NekoItemStack = realize0(nekoItem, crate)

    private fun realize0(nekoItem: NekoItem, any: Any): NekoItemStack {
        val context = SchemeGenerationContext(SchemaGenerationTrigger.wrap(any))
        val nekoStack = createItemStack0(nekoItem, context)
        return nekoStack
    }

    /**
     * Creates a NekoStack with the [context].
     *
     * @param context the input context
     * @return a new once-off NekoStack
     */
    private fun createItemStack0(nekoItem: NekoItem, context: SchemeGenerationContext): NekoItemStack {
        val nekoStack = run {
            val namespacedKey = nekoItem.material.asNamespacedKey
            val material = requireNotNull(Registry.MATERIAL.get(namespacedKey)) { "Can't find material with key '{${nekoItem.material}'" }
            NekoItemStackFactory.new(material)
        }

        // write base data
        nekoStack.putKey(nekoItem.key)
        nekoStack.putSeed(context.seed)

        // write item meta
        with(nekoStack.metadata) {
            // the order of meta population is hardcoded currently
            // TODO configurable order of meta population

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

        // write item cells
        nekoItem.cells.forEach { (id, schema) ->
            // the order of cell population should be the same as
            // that they are declared in the configuration list

            val cell = BinaryCellFactory.generate(context, schema)
            if (cell != null) {
                // if the binary cell is non-null, it's either:
                // 1) a cell with some content, or
                // 2) a cell with no content + keepEmpty is true
                nekoStack.cells.put(id, cell)
            }
            // if it's null, simply don't put the cell
        }

        return nekoStack
    }

    /**
     * Generates meta from [nekoItem] and [context]
     * and then writes it to [ItemMetaHolder].
     *
     * **Only if something is generated will the item meta be written out.**
     *
     * @param nekoItem the item
     * @param context the context
     * @param V the type of item meta value, shared by [S] and [B]
     * @param S the type of [SchemeItemMeta]
     * @param B the type of [BinaryItemMeta]
     */
    private inline fun <V, reified S : SchemeItemMeta<V>, reified B : BinaryItemMeta<V>> ItemMetaHolder.generateAndSet(
        nekoItem: NekoItem,
        context: SchemeGenerationContext,
    ) {
        val meta = nekoItem.getItemMetaBy<S>()
        val value = meta.generate(context)
        if (value != null) {
            // write the item meta only if something is generated
            set<B, V>(value)
        }
    }

}