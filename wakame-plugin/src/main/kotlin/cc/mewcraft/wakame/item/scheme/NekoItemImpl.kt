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
        // create a blank WakaItemStack
        val materialMeta = getSchemeMetaByClass<MaterialMeta>()
        val material = materialMeta.generate(context)
        val nekoStack = NekoItemStackFactory.new(material)

        with(nekoStack.metaAccessor) {
            // Side note:
            // the order of meta population is hardcoded currently
            // TODO make the order of meta population configurable

            generateAndSet<DisplayNameMeta, Component>(context, ItemMetaSetter::putName)
            generateAndSet<LoreMeta, List<Component>>(context, ItemMetaSetter::putLore)
            generateAndSet<LevelMeta, Int>(context, ItemMetaSetter::putLevel)
            generateAndSet<RarityMeta, Rarity>(context, ItemMetaSetter::putRarity)
            generateAndSet<ElementMeta, Set<Element>>(context, ItemMetaSetter::putElements)
            generateAndSet<KizamiMeta, Set<Kizami>>(context, ItemMetaSetter::putKizami)
            generateAndSet<SkinMeta, ItemSkin>(context, ItemMetaSetter::putSkin)
            generateAndSet<SkinOwnerMeta, UUID>(context, ItemMetaSetter::putSkinOwner)
        }

        // Side note:
        // the order of cell population should be the same as
        // that they are declared in the YAML list
        schemeCells.forEach { (id, scheme) ->
            val binary = BinaryCellFactory.generate(context, scheme)
            if (binary != null) {
                // if it's non-null, then it's either:
                // 1) a cell with some content, or
                // 2) a cell with no content + keepEmpty is true
                nekoStack.cellAccessor.put(id, binary)
            }
            // if it's null, simply don't put the cell
        }

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
    private inline fun <reified S : SchemeMeta<T>, T> ItemMetaSetter.generateAndSet(
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