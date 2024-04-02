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

@JvmInline
internal value class ItemMetaAccessorImpl(
    val base: NekoStackImpl,
) : KoinComponent, ItemMetaAccessor {
    override val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoNamespaces.ITEM_META)
    override val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoNamespaces.ITEM_META, CompoundShadowTag::create)

    override val snapshot: Set<BinaryItemMeta<*>>
        get() {
            val root = rootOrNull ?: return emptySet()
            val ret = ObjectArraySet<BinaryItemMeta<*>>(8)

            // TODO optimize the efficiency
            //  solution: loop through the root tag, then use the tag key to get implementation,
            //  instead of looping through the whole meta registry and call contains()

            // check the existence of each item meta
            // if one exists, we add it to the map
            /*
            ItemMetaRegistry.Binary.reflections().forEach { (_, companion, constructor) ->
                if (companion.contains(root)) {
                    ret += constructor(this)
                }
            }
            */

            // loop through the keySet of the root compound tag,
            // then use the tag key to get corresponding accessor
            root.keySet().forEach { key ->
                val constructor = ItemMetaRegistry.Binary.reflectionLookup(key).constructor
                val itemMeta = constructor(this)
                ret += itemMeta
            }

            return ret
        }

    override fun <M : BinaryItemMeta<*>> getAccessor(clazz: KClass<out M>): M {
        val (_, _, constructor) = ItemMetaRegistry.Binary.reflectionLookup(clazz)
        val itemMeta = constructor(this)
        @Suppress("UNCHECKED_CAST")
        return (itemMeta as M)
    }
}

/**
 * 该实现仅用来创建“空的” [BinaryItemMeta]，没有任何其他作用。
 */
internal object ItemMetaAccessorNoop : ItemMetaAccessor {
    override val rootOrNull: CompoundShadowTag get() = throw UnsupportedOperationException()
    override val rootOrCreate: CompoundShadowTag get() = throw UnsupportedOperationException()
    override val snapshot: Set<BinaryItemMeta<*>> get() = throw UnsupportedOperationException()
    override fun <M : BinaryItemMeta<*>> getAccessor(clazz: KClass<out M>): M = throw UnsupportedOperationException()
}