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

    override val map: Map<KClass<out ItemMeta<*>>, ItemMeta<*>>
        get() {
            val root = rootOrNull ?: return emptyMap()
            // check the existence of each item meta
            // if one exists, we add it to the map

            val ret = Reference2ObjectArrayMap<KClass<out ItemMeta<*>>, ItemMeta<*>>()
            ItemMetaRegistry.reflections().forEach { (clazz, companion, constructor) ->
                if (companion.contains(root)) {
                    ret[clazz] = constructor.invoke(this) as ItemMeta<*>?
                }
            }
            return ret
        }

    override fun <T : ItemMeta<*>> get(clazz: KClass<out T>): T? {
        val root = rootOrNull ?: return null
        val (_, companion, constructor) = ItemMetaRegistry.reflect(clazz)
        return if (companion.contains(root)) {
            @Suppress("UNCHECKED_CAST")
            constructor.invoke(this) as T
        } else {
            null
        }
    }

    override fun <T : ItemMeta<V>, V> set(clazz: KClass<out T>, value: V) {
        val (_, _, constructor) = ItemMetaRegistry.reflect(clazz)
        val itemMeta = @Suppress("UNCHECKED_CAST") (constructor.invoke(this) as T)
        itemMeta.set(value)
    }

    override fun <T : ItemMeta<*>> remove(clazz: KClass<out T>) {
        val (_, _, constructor) = ItemMetaRegistry.reflect(clazz)
        val itemMeta = @Suppress("UNCHECKED_CAST") (constructor.invoke(this) as T)
        itemMeta.remove()
    }
}
