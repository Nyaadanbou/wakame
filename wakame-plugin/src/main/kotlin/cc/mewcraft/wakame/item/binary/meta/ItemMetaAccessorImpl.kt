package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.binary.BaseNekoStack
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.registry.ItemMetaRegistry
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

@JvmInline
internal value class ItemMetaAccessorImpl(
    override val item: BaseNekoStack,
) : KoinComponent, ItemMetaAccessor {
    override val rootOrNull: CompoundShadowTag?
        get() = item.tags.getCompoundOrNull(Namespaces.ITEM_META)
    override val rootOrCreate: CompoundShadowTag
        get() = item.tags.getOrPut(Namespaces.ITEM_META, CompoundShadowTag::create)

    override val snapshot: Set<BinaryItemMeta<*>>
        get() {
            val root = rootOrNull ?: return emptySet()
            val ret = ObjectArraySet<BinaryItemMeta<*>>(8)

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
        val (_, constructor) = ItemMetaRegistry.Binary.reflectionLookup(clazz)
        val itemMeta = constructor(this)
        @Suppress("UNCHECKED_CAST")
        return (itemMeta as M)
    }
}

/**
 * 该实现仅用来创建“空的” [BinaryItemMeta]，没有任何其他作用。
 */
internal object ItemMetaAccessorNoop : ItemMetaAccessor {
    override val item: NekoStack get() = throw UnsupportedOperationException()
    override val rootOrNull: CompoundShadowTag get() = throw UnsupportedOperationException()
    override val rootOrCreate: CompoundShadowTag get() = throw UnsupportedOperationException()
    override val snapshot: Set<BinaryItemMeta<*>> get() = throw UnsupportedOperationException()
    override fun <M : BinaryItemMeta<*>> getAccessor(clazz: KClass<out M>): M = throw UnsupportedOperationException()
}