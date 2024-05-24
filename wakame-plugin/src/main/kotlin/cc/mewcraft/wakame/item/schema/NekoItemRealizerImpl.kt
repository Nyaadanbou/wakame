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
import kotlin.reflect.KClass

internal class NekoItemRealizerImpl : NekoItemRealizer {
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
        val stack = run {
            val key = item.material.asBukkit
            val mat = requireNotNull(Registry.MATERIAL.get(key)) { "Can't find material by key '${item.material}'" }
            PlayNekoStackFactory.new(mat)
        }

        //
        // Write base data
        //
        stack.key = item.key
        stack.variant = 0

        //
        // Write item meta
        //
        ItemMetaWriter {
            // Caution: the order of the generation matters here!!!
            // Caution: 这里每个 statement 的执行顺序很重要，存在依赖关系!!!

            bind<SCustomNameMeta, BCustomNameMeta>()
            bind<SLoreMeta, BLoreMeta>()
            bind<SDurabilityMeta, BDurabilityMeta>()
            bind<SLevelMeta, BLevelMeta>()
            bind<SRarityMeta, BRarityMeta>()
            bind<SElementMeta, BElementMeta>()
            bind<SKizamiMeta, BKizamiMeta>()
            bind<SSkinMeta, BSkinMeta>()
            bind<SSkinOwnerMeta, BSkinOwnerMeta>()

        }.write(item, stack, context)

        //
        // Write item cell
        //
        ItemCellWriter.write(item, stack, context)

        return stack
    }
}

/**
 * A constructor function to create [ItemMetaWriter].
 */
private fun ItemMetaWriter(block: ItemMetaWriter.() -> Unit): ItemMetaWriter {
    return ItemMetaWriter().apply(block)
}

/**
 * Responsible to write item meta to an item.
 */
private class ItemMetaWriter {
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
     * Writes all item meta specified by [bindings] to the item [stack].
     *
     * @param item the template item
     * @param stack the world-state item
     * @param context the generation context
     */
    fun write(item: NekoItem, stack: NekoStack, context: SchemaGenerationContext) {
        bindings.forEach {
            val schemaItemMetaKClass = it.schemaItemMetaKClass
            val binaryItemMetaKClass = it.binaryItemMetaKClass

            // Get the schema item meta
            val meta = item.getMeta(schemaItemMetaKClass)
            // Generate a result from the schema item meta
            val value = meta.generate(context)
            // If something is generated, write the result to the item
            if (value is GenerationResult.Thing) {
                // Get the accessor to the binary item meta
                val accessor = stack.meta.getAccessor(binaryItemMetaKClass)
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

/**
 * Responsible to write item cells to an item.
 */
private object ItemCellWriter {
    fun write(item: NekoItem, stack: NekoStack, context: SchemaGenerationContext) {
        item.cellMap.forEach { (id, schemaCell) ->
            // The order of cell population should be the same as
            // that they are declared in the configuration file.

            val binaryCell = BinaryCellFactory.reify(schemaCell, context)
            if (!binaryCell.isNoop) {
                // We only write the cell if it's not a noop.
                stack.cell.put(id, binaryCell)
            }
        }
    }
}
