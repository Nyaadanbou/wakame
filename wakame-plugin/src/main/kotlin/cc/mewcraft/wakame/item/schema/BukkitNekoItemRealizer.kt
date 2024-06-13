package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackImplementation
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.cell.isNoop
import cc.mewcraft.wakame.item.binary.createNekoStack
import cc.mewcraft.wakame.item.binary.meta.BCustomNameMeta
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.binary.meta.BElementMeta
import cc.mewcraft.wakame.item.binary.meta.BKizamiMeta
import cc.mewcraft.wakame.item.binary.meta.BLevelMeta
import cc.mewcraft.wakame.item.binary.meta.BLoreMeta
import cc.mewcraft.wakame.item.binary.meta.BRarityMeta
import cc.mewcraft.wakame.item.binary.meta.BSkinMeta
import cc.mewcraft.wakame.item.binary.meta.BSkinOwnerMeta
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.set
import cc.mewcraft.wakame.item.schema.meta.GenerationResult
import cc.mewcraft.wakame.item.schema.meta.SCustomNameMeta
import cc.mewcraft.wakame.item.schema.meta.SDurabilityMeta
import cc.mewcraft.wakame.item.schema.meta.SElementMeta
import cc.mewcraft.wakame.item.schema.meta.SKizamiMeta
import cc.mewcraft.wakame.item.schema.meta.SLevelMeta
import cc.mewcraft.wakame.item.schema.meta.SLoreMeta
import cc.mewcraft.wakame.item.schema.meta.SRarityMeta
import cc.mewcraft.wakame.item.schema.meta.SSkinMeta
import cc.mewcraft.wakame.item.schema.meta.SSkinOwnerMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.asBukkit
import org.bukkit.Registry
import org.bukkit.inventory.ItemFlag
import kotlin.reflect.KClass

internal class BukkitNekoItemRealizer : NekoItemRealizer {
    override fun realize(item: NekoItem, context: SchemaGenerationContext): PlayNekoStack {
        return createItemStack0(item, context)
    }

    override fun realize(item: NekoItem, user: User<*>): PlayNekoStack {
        return realize0(item, user)
    }

    override fun realize(item: NekoItem, crate: Crate): PlayNekoStack {
        return realize0(item, crate)
    }

    private fun realize0(item: NekoItem, source: Any): PlayNekoStack {
        val context = SchemaGenerationContext(SchemaGenerationTrigger.wrap(source))
        val nekoStack = createItemStack0(item, context)
        return nekoStack
    }

    /**
     * Creates a NekoStack with the [context].
     *
     * @param blueprint the item blueprint
     * @param context the input context
     * @return a new once-off NekoStack
     */
    private fun createItemStack0(blueprint: NekoItem, context: SchemaGenerationContext): PlayNekoStack {
        val nekoStack = run {
            val key = blueprint.material.asBukkit
            val mat = requireNotNull(Registry.MATERIAL.get(key)) { "Can't find org.bukkit.Material by `${blueprint.material}`" }
            mat.createNekoStack()
        }

        /* Write basic data */

        NekoStackImplementation.setKey(nekoStack.tags, blueprint.key)
        nekoStack.variant = 0
        nekoStack.itemStack.apply {
            val itemMeta = itemMeta

            if (blueprint.hideTooltip) {
                itemMeta.isHideTooltip = true
            }

            if (blueprint.hideAdditionalTooltip) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }

            blueprint.shownInTooltip.apply(itemMeta)

            this.itemMeta = itemMeta
        }

        /* Write item meta data */

        ItemMetaApplicator {
            // Caution: the order of the generation matters here!
            bind<SCustomNameMeta, BCustomNameMeta>()
            bind<SLoreMeta, BLoreMeta>()
            bind<SDurabilityMeta, BDurabilityMeta>()
            bind<SLevelMeta, BLevelMeta>()
            bind<SRarityMeta, BRarityMeta>()
            bind<SElementMeta, BElementMeta>()
            bind<SKizamiMeta, BKizamiMeta>()
            bind<SSkinMeta, BSkinMeta>()
            bind<SSkinOwnerMeta, BSkinOwnerMeta>()
        }.write(blueprint, nekoStack, context)

        /* Write item cell data */

        blueprint.cellMap.forEach { (id, schemaCell) ->
            // The order of cell population should be the same as
            // that they are declared in the configuration file.

            val binaryCell = BinaryCellFactory.reify(schemaCell, context)
            if (!binaryCell.isNoop) {
                // We only write the cell if it's not a noop.
                nekoStack.cell.put(id, binaryCell)
            }
        }

        return nekoStack
    }
}


/**
 * Responsible to write item meta to an item.
 */
private class ItemMetaApplicator {

    /**
     * A constructor function to create [ItemMetaApplicator].
     */
    constructor(block: ItemMetaApplicator.() -> Unit) {
        this.apply(block)
    }

    // We have to use a list to store the bindings, as
    // the schema item meta have to be generated in a
    // predictable order, and therefore they follow the
    // order as they are listed in the list of bindings.

    // Why is that? because some item meta **depends on**
    // the results of some specific item meta.

    private val bindings: MutableList<Binding<*, *>> = mutableListOf()

    /**
     * Adds an item meta binding to the writer, **in order**.
     *
     * @param S the type of [SchemaItemMeta]
     * @param B the type of [BinaryItemMeta] corresponding to [S]
     */
    inline fun <reified S : SchemaItemMeta<*>, reified B : BinaryItemMeta<*>> bind() {
        bindings += Binding(S::class, B::class)
    }

    /**
     * Writes all item meta specified by [bindings] to the item [nekoStack].
     *
     * @param blueprint the template item
     * @param nekoStack the world-state item
     * @param context the generation context
     */
    fun write(blueprint: NekoItem, nekoStack: NekoStack, context: SchemaGenerationContext) {
        bindings.forEach {
            val schemaItemMetaKClass = it.schemaItemMetaKClass
            val binaryItemMetaKClass = it.binaryItemMetaKClass

            // Get the schema item meta
            val meta = blueprint.getMeta(schemaItemMetaKClass)
            // Generate a result from the schema item meta
            val value = meta.generate(context)
            // If something is generated, write the result to the item
            if (value is GenerationResult.Thing) {
                // Get the accessor to the binary item meta
                val accessor = nekoStack.meta.getAccessor(binaryItemMetaKClass)
                // Write the result to the binary item meta
                accessor.set(value.value)
            }
        }
    }

    /**
     * A binding between a [SchemaItemMeta] and a [BinaryItemMeta].
     *
     * @param S the type of [SchemaItemMeta]
     * @param B the type of [BinaryItemMeta] corresponding to [S]
     */
    data class Binding<S : SchemaItemMeta<*>, B : BinaryItemMeta<*>>(
        val schemaItemMetaKClass: KClass<S>,
        val binaryItemMetaKClass: KClass<B>,
    )
}
