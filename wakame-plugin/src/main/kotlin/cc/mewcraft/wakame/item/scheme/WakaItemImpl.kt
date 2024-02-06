package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.WakaItemStack
import cc.mewcraft.wakame.item.binary.WakaItemStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.meta.ItemMetaSetter
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID

internal class WakaItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val schemeMeta: Map<Key, SchemeMeta<*>>,
    override val schemeCells: Map<String, SchemeCell>,
) : WakaItem {
    override fun createItemStack(player: Player?): WakaItemStack {
        // create a blank generation context
        // TODO("actually reads the player's level")
        val context = SchemeGenerationContext(player?.level ?: 1)
        val nekoStack = createItemStack0(context)
        return nekoStack
    }

    override fun createItemStack(crate: BinaryCrate): WakaItemStack {
        // create a blank generation context
        val context = SchemeGenerationContext(crate.level)
        val nekoStack = createItemStack0(context)
        return nekoStack
    }

    /**
     * Creates a NekoStack with the given [context].
     *
     * @param context the input context
     * @return a new NekoStack
     */
    private fun createItemStack0(context: SchemeGenerationContext): WakaItemStack {
        // create a blank WakaItemStack
        val materialMeta = getSchemeMetaByClass<MaterialMeta>()
        val material = materialMeta.generate(context)
        val nekoStack = WakaItemStackFactory.new(material)

        //<editor-fold desc="Sets Item Meta">
        with(nekoStack.metaAccessor) {
            // TODO make the order of meta population configurable
            // (by alphabet order)
            generateAndSetItemMeta<DisplayNameMeta, Component>(context, ItemMetaSetter::putName)
            generateAndSetItemMeta<ElementMeta, Set<Element>>(context, ItemMetaSetter::putElements)
            generateAndSetItemMeta<KizamiMeta, Set<Kizami>>(context, ItemMetaSetter::putKizami)
            generateAndSetItemMeta<LevelMeta, Int>(context, ItemMetaSetter::putLevel)
            generateAndSetItemMeta<LoreMeta, List<Component>>(context, ItemMetaSetter::putLore)
            generateAndSetItemMeta<RarityMeta, Rarity>(context, ItemMetaSetter::putRarity)
            generateAndSetItemMeta<SkinMeta, ItemSkin>(context, ItemMetaSetter::putSkin)
            generateAndSetItemMeta<SkinOwnerMeta, UUID>(context, ItemMetaSetter::putSkinOwner)
        }
        //</editor-fold>

        //<editor-fold desc="Sets Item Cells">
        with(nekoStack.cellAccessor) {
            schemeCells.forEach { (cellId, schemeCell) ->
                val binaryCell = BinaryCellFactory.generate(context, schemeCell)
                if (binaryCell != null) {
                    // if it's non-null, then it's either:
                    //   1) a cell with some content, or
                    //   2) a cell with no content + keepEmpty is true
                    put(cellId, binaryCell)
                }
                // if it's null, simply don't put the cell
            }
        }
        //</editor-fold>

        //<editor-fold desc="Sets Item Statistics">
        with(nekoStack.statsAccessor) {
            // TODO probably we don't have to write the `stats` tag here
        }
        //</editor-fold>

        return nekoStack
    }

    /**
     * A convenient function.
     *
     * @param context the context
     * @param setter a member function of [ItemMetaSetter]
     * @param S the type of [SchemeMeta]
     * @param T the type of item meta value
     * @receiver the [ItemMetaSetter] which the function executes on
     */
    private inline fun <reified S : SchemeMeta<T>, T> ItemMetaSetter.generateAndSetItemMeta(
        context: SchemeGenerationContext,
        setter: ItemMetaSetter.(T) -> Any,
    ) {
        val meta: S = getSchemeMetaByClass<S>()
        val value: T? = meta.generate(context)
        if (value != null) {
            // set the meta only if something is generated
            setter(value)
        }
    }
}