package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.binary.NekoStackImpl
import cc.mewcraft.wakame.registry.ItemMetaRegistry
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

// TODO Make it a value class
//  To achieve this, we need to get rid of the reflection
internal class ItemMetaAccessorImpl(
    val base: NekoStackImpl,
) : KoinComponent, ItemMetaAccessor {
    override val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoNamespaces.ITEM_META)
    override val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoNamespaces.ITEM_META, CompoundShadowTag::create)

    override val snapshot: Set<BinaryItemMeta<*>>
        get() {
            val root = rootOrNull ?: return emptySet()
            val ret = ObjectArraySet<BinaryItemMeta<*>>()

            // TODO optimize the efficiency
            //  solution: loop through the root tag, then use the tag key to get implementation,
            //  instead of looping through the whole meta registry and call contains()

            // check the existence of each item meta
            // if one exists, we add it to the map
            ItemMetaRegistry.reflections().forEach { (_, companion, constructor) ->
                if (companion.contains(root)) {
                    ret += constructor(this)
                }
            }

            return ret
        }

    override fun <M : BinaryItemMeta<*>> getAccessor(clazz: KClass<out M>): M {
        val (_, _, constructor) = ItemMetaRegistry.reflectionLookup(clazz)
        val itemMeta = constructor(this)
        @Suppress("UNCHECKED_CAST")
        return (itemMeta as M)
    }
}
