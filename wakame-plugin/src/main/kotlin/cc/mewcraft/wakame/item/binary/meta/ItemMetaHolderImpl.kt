package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.binary.NekoItemStackImpl
import cc.mewcraft.wakame.registry.ItemMetaRegistry
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

internal class ItemMetaHolderImpl(
    private val base: NekoItemStackImpl,
) : KoinComponent, ItemMetaHolder {
    val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoNamespaces.ITEM_META)
    val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoNamespaces.ITEM_META, CompoundShadowTag::create)

    override val map: Map<KClass<out BinaryItemMeta<*>>, BinaryItemMeta<*>>
        get() {
            val root = rootOrNull ?: return emptyMap()
            // check the existence of each item meta
            // if one exists, we add it to the map

            val ret = Reference2ObjectArrayMap<KClass<out BinaryItemMeta<*>>, BinaryItemMeta<*>>()
            ItemMetaRegistry.reflections().forEach { (clazz, companion, constructor) ->
                if (companion.contains(root)) {
                    ret[clazz] = constructor.invoke(this) as BinaryItemMeta<*>?
                }
            }
            return ret
        }

    override fun <T : BinaryItemMeta<V>, V> get(clazz: KClass<out T>): V? {
        val root = rootOrNull ?: return null
        val (_, companion, constructor) = ItemMetaRegistry.reflect(clazz)
        return if (companion.contains(root)) {
            val itemMeta = constructor.invoke(this)
            @Suppress("UNCHECKED_CAST")
            (itemMeta as T).getOrNull()
        } else {
            null
        }
    }

    override fun <T : BinaryItemMeta<V>, V> set(clazz: KClass<out T>, value: V) {
        val (_, _, constructor) = ItemMetaRegistry.reflect(clazz)
        val itemMeta = constructor.invoke(this)
        @Suppress("UNCHECKED_CAST")
        (itemMeta as T).set(value)
    }

    override fun <T : BinaryItemMeta<*>> remove(clazz: KClass<out T>) {
        val (_, _, constructor) = ItemMetaRegistry.reflect(clazz)
        val itemMeta = constructor.invoke(this)
        @Suppress("UNCHECKED_CAST")
        (itemMeta as T).remove()
    }
}
