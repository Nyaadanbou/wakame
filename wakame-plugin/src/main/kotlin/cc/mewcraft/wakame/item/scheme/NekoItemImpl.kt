package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaHolder
import cc.mewcraft.wakame.item.binary.meta.set
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.MaterialMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import java.util.UUID
import cc.mewcraft.wakame.item.binary.meta.DisplayLoreMeta as BDisplayLoreMeta
import cc.mewcraft.wakame.item.binary.meta.DisplayNameMeta as BDisplayNameMeta
import cc.mewcraft.wakame.item.binary.meta.ElementMeta as BElementMeta
import cc.mewcraft.wakame.item.binary.meta.KizamiMeta as BKizamiMeta
import cc.mewcraft.wakame.item.binary.meta.LevelMeta as BLevelMeta
import cc.mewcraft.wakame.item.binary.meta.RarityMeta as BRarityMeta
import cc.mewcraft.wakame.item.binary.meta.SkinMeta as BSkinMeta
import cc.mewcraft.wakame.item.binary.meta.SkinOwnerMeta as BSkinOwnerMeta
import cc.mewcraft.wakame.item.scheme.meta.DisplayLoreMeta as SDisplayLoreMeta
import cc.mewcraft.wakame.item.scheme.meta.DisplayNameMeta as SDisplayNameMeta
import cc.mewcraft.wakame.item.scheme.meta.ElementMeta as SElementMeta
import cc.mewcraft.wakame.item.scheme.meta.KizamiMeta as SKizamiMeta
import cc.mewcraft.wakame.item.scheme.meta.LevelMeta as SLevelMeta
import cc.mewcraft.wakame.item.scheme.meta.RarityMeta as SRarityMeta
import cc.mewcraft.wakame.item.scheme.meta.SkinMeta as SSkinMeta
import cc.mewcraft.wakame.item.scheme.meta.SkinOwnerMeta as SSkinOwnerMeta

internal data class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val itemMeta: Map<Key, SchemeItemMeta<*>>,
    override val cells: Map<String, SchemeCell>,
) : NekoItem {
    override fun createItemStack(player: Player?): NekoItemStack {
        val context = SchemeGenerationContext(playerObject = player)
        val nekoStack = createItemStack0(context)
        return nekoStack
    }

    override fun createItemStack(crate: BinaryCrate): NekoItemStack {
        val context = SchemeGenerationContext(crateObject = crate)
        val nekoStack = createItemStack0(context)
        return nekoStack
    }

    /**
     * Creates a NekoStack with the given [context].
     *
     * @param context the input context
     * @return a new NekoStack
     */
    private fun createItemStack0(context: SchemeGenerationContext): NekoItemStack {
        // create a blank NekoItemStack
        val materialMeta = getItemMetaByClass<MaterialMeta>()
        val material = materialMeta.generate(context)
        val nekoStack = NekoItemStackFactory.new(material)

        // put key
        nekoStack.putKey(key)

        // put item meta
        with(nekoStack.metadata) {
            // Side note:
            // the order of meta population is hardcoded currently
            // TODO make the order of meta population configurable

            generateAndSet<String, SDisplayNameMeta, BDisplayNameMeta>(context)
            generateAndSet<List<String>, SDisplayLoreMeta, BDisplayLoreMeta>(context)
            generateAndSet<Int, SLevelMeta, BLevelMeta>(context)
            generateAndSet<Rarity, SRarityMeta, BRarityMeta>(context)
            generateAndSet<Set<Element>, SElementMeta, BElementMeta>(context)
            generateAndSet<Set<Kizami>, SKizamiMeta, BKizamiMeta>(context)
            generateAndSet<ItemSkin, SSkinMeta, BSkinMeta>(context)
            generateAndSet<UUID, SSkinOwnerMeta, BSkinOwnerMeta>(context)
        }

        // put cells
        cells.forEach { (id, scheme) ->
            // Side note:
            // the order of cell population should be the same as
            // that they are declared in the YAML list

            val binary = BinaryCellFactory.generate(context, scheme)
            if (binary != null) {
                // if it's non-null, then it's either:
                // 1) a cell with some content, or
                // 2) a cell with no content + keepEmpty is true
                nekoStack.cells.put(id, binary)
            } else {
                // if it's null, simply don't put the cell
            }
        }

        return nekoStack
    }

    /**
     * Generates meta and then sets it. **Only if something is generated will
     * the item meta be put in the item.** Otherwise, this function does
     * nothing to the item.
     *
     * @param context the context
     * @param ST the type of [SchemeItemMeta]
     * @param T the type of item meta value
     */
    private inline fun <T, reified ST : SchemeItemMeta<T>, reified BT : BinaryItemMeta<T>> ItemMetaHolder.generateAndSet(
        context: SchemeGenerationContext,
    ) {
        val meta = getItemMetaByClass<ST>()
        val value = meta.generate(context)
        if (value != null) {
            // set the meta only if something is generated
            set<BT, T>(value)
        }
    }
}