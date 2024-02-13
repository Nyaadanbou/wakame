package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
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

internal class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val schemeMeta: Map<Key, SchemeMeta<*>>,
    override val schemeCells: Map<String, SchemeCell>,
) : NekoItem {
    override fun createItemStack(player: Player?): NekoItemStack {
        // create a blank generation context
        // TODO("actually reads the player's adventure level")
        val context = SchemeGenerationContext(player?.level ?: 1)
        val nekoStack = createItemStack0(context)
        return nekoStack
    }

    override fun createItemStack(crate: BinaryCrate): NekoItemStack {
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
    private fun createItemStack0(context: SchemeGenerationContext): NekoItemStack {
        // create a blank WakaItemStack
        val materialMeta = getSchemeMetaByClass<MaterialMeta>()
        val material = materialMeta.generate(context)
        val nekoStack = NekoItemStackFactory.new(material)

        //<editor-fold desc="Sets Item Meta">
        with(nekoStack.metaAccessor) {
            // Side note:
            // the order of meta population is hardcoded currently
            // TODO make the order of meta population configurable

            generateAndSetItemMeta<DisplayNameMeta, Component>(context, ItemMetaSetter::putName)
            generateAndSetItemMeta<LoreMeta, List<Component>>(context, ItemMetaSetter::putLore)
            generateAndSetItemMeta<LevelMeta, Int>(context, ItemMetaSetter::putLevel)
            generateAndSetItemMeta<RarityMeta, Rarity>(context, ItemMetaSetter::putRarity)
            generateAndSetItemMeta<ElementMeta, Set<Element>>(context, ItemMetaSetter::putElements)
            generateAndSetItemMeta<KizamiMeta, Set<Kizami>>(context, ItemMetaSetter::putKizami)
            generateAndSetItemMeta<SkinMeta, ItemSkin>(context, ItemMetaSetter::putSkin)
            generateAndSetItemMeta<SkinOwnerMeta, UUID>(context, ItemMetaSetter::putSkinOwner)
        }
        //</editor-fold>

        //<editor-fold desc="Sets Item Cells">
        with(nekoStack.cellAccessor) {
            // Side note:
            // the order of cell population should be the same as
            // that they are declared in the YAML list
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

        return nekoStack
    }

    /**
     * Generates meta and then sets it. **Only if something is generated will
     * the item meta be put in the item.** Otherwise, this function does
     * nothing to the item.
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