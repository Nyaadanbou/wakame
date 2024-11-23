package cc.mewcraft.wakame.shadow.inventory

import cc.mewcraft.nbt.Tag
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.lucko.shadow.*
import me.lucko.shadow.ShadowingStrategy.*
import me.lucko.shadow.Target
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.bukkit.ObcClassTarget
import java.util.AbstractMap.SimpleEntry
import java.util.TreeMap

@ObcClassTarget("inventory.CraftMetaItem")
interface ShadowCraftMetaItem : Shadow {
    @get:Field
    @get:Target("unhandledTags")
    @get:ShadowingStrategy(wrapper = UnhandledTagsShadowingStrategy::class)
    val unhandledTags: MutableMap<String, Tag>
}

//
// Strategy implementations
//

private typealias NmsTag = Any

@Suppress("UNCHECKED_CAST")
private class UnhandledTagsShadowingStrategy : Wrapper, Unwrapper {
    override fun wrap(unwrapped: Any?, expectedType: Class<*>, shadowFactory: ShadowFactory): Any {
        requireNotNull(unwrapped)
        return UnhandledTagsWrapper(unwrapped as TreeMap<String, NmsTag>)
    }

    override fun unwrap(wrapped: Any?, expectedType: Class<*>, shadowFactory: ShadowFactory): Any {
        requireNotNull(wrapped)
        return (wrapped as UnhandledTagsWrapper).handle
    }

    override fun unwrap(wrappedClass: Class<*>?, shadowFactory: ShadowFactory): Class<*> {
        return shadowFactory.targetClass(requireNotNull(wrappedClass))
    }
}

//
// Helper classes
//

private val NmsTag.wrap: Tag
    get() = BukkitShadowFactory.global().shadow<Tag>(this)
private val Tag.unwrap: NmsTag
    get() = shadowTarget as NmsTag

private class UnhandledTagsWrapper(
    val handle: TreeMap<String, NmsTag>,
) : MutableMap<String, Tag> {
    override val keys: MutableSet<String> = handle.keys
    override val values: MutableCollection<Tag> = MapValuesWrapper(handle.values)
    override val entries: MutableSet<MutableMap.MutableEntry<String, Tag>> = MapEntriesWrapper(handle.entries)
    override val size: Int get() = handle.size
    override fun isEmpty(): Boolean = handle.isEmpty()
    override fun containsKey(key: String): Boolean = handle.containsKey(key)
    override fun containsValue(value: Tag): Boolean = handle.containsValue(value.unwrap)
    override fun get(key: String): Tag? = handle[key]?.wrap
    override fun put(key: String, value: Tag): Tag? = handle.put(key, value.unwrap)?.wrap
    override fun remove(key: String): Tag? = handle.remove(key)?.wrap
    override fun putAll(from: Map<out String, Tag>) = handle.putAll(from.mapValues { (_, v) -> v.unwrap })
    override fun clear() = handle.clear()
}

private class MapValuesWrapper(
    private val handle: MutableCollection<NmsTag>,
) : MutableCollection<Tag> {
    override val size: Int get() = handle.size
    override fun isEmpty(): Boolean = handle.isEmpty()
    override fun contains(element: Tag): Boolean = handle.contains(element.unwrap)
    override fun containsAll(elements: Collection<Tag>): Boolean = handle.containsAll(elements.unwrapCollection)
    override fun iterator(): MutableIterator<Tag> = MutableIteratorWrapper(handle.iterator())
    override fun add(element: Tag): Boolean = handle.add(element.unwrap)
    override fun remove(element: Tag): Boolean = handle.remove(element.unwrap)
    override fun addAll(elements: Collection<Tag>): Boolean = handle.addAll(elements.unwrapCollection)
    override fun removeAll(elements: Collection<Tag>): Boolean = handle.removeAll(elements.unwrapCollection)
    override fun retainAll(elements: Collection<Tag>): Boolean = handle.retainAll(elements.unwrapCollection)
    override fun clear() = handle.clear()

    private val Collection<Tag>.unwrapCollection: MutableSet<NmsTag>
        get() = this.mapTo(ObjectOpenHashSet()) { it.unwrap }

    private class MutableIteratorWrapper(
        private val handle: MutableIterator<NmsTag>,
    ) : MutableIterator<Tag> {
        override fun next(): Tag = handle.next().wrap
        override fun hasNext(): Boolean = handle.hasNext()
        override fun remove() = handle.remove()
    }
}

private typealias SimpleEntry<K, V> = java.util.AbstractMap.SimpleEntry<K, V>

private class MapEntriesWrapper(
    private val handle: MutableSet<MutableMap.MutableEntry<String, NmsTag>>,
) : MutableSet<MutableMap.MutableEntry<String, Tag>> {
    override val size: Int get() = handle.size
    override fun isEmpty(): Boolean = handle.isEmpty()
    override fun contains(element: MutableMap.MutableEntry<String, Tag>): Boolean = handle.contains(element.unwrapEntry)
    override fun containsAll(elements: Collection<MutableMap.MutableEntry<String, Tag>>): Boolean = handle.containsAll(elements.unwrapEntryCollection)
    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, Tag>> = MutableIteratorWrapper(handle.iterator())
    override fun add(element: MutableMap.MutableEntry<String, Tag>): Boolean = handle.add(element.unwrapEntry)
    override fun remove(element: MutableMap.MutableEntry<String, Tag>): Boolean = handle.remove(element.unwrapEntry)
    override fun addAll(elements: Collection<MutableMap.MutableEntry<String, Tag>>): Boolean = handle.addAll(elements.unwrapEntryCollection)
    override fun removeAll(elements: Collection<MutableMap.MutableEntry<String, Tag>>): Boolean = handle.removeAll(elements.unwrapEntryCollection)
    override fun retainAll(elements: Collection<MutableMap.MutableEntry<String, Tag>>): Boolean = handle.retainAll(elements.unwrapEntryCollection)
    override fun clear() = handle.clear()

    private class MutableIteratorWrapper(
        private val handle: MutableIterator<MutableMap.MutableEntry<String, NmsTag>>,
    ) : MutableIterator<MutableMap.MutableEntry<String, Tag>> {
        override fun next(): MutableMap.MutableEntry<String, Tag> = handle.next().wrapEntry
        override fun hasNext(): Boolean = handle.hasNext()
        override fun remove() = handle.remove()

        private val MutableMap.MutableEntry<String, NmsTag>.wrapEntry: MutableMap.MutableEntry<String, Tag>
            get() {
                val key: String = this.key
                val value: Tag = this.value.wrap
                return SimpleEntry(key, value)
            }
    }

    private val MutableMap.MutableEntry<String, Tag>.unwrapEntry: MutableMap.MutableEntry<String, NmsTag>
        get() {
            val key: String = this.key
            val value: NmsTag = this.value.unwrap
            val entry = SimpleEntry(key, value)
            return entry
        }

    private val Collection<MutableMap.MutableEntry<String, Tag>>.unwrapEntryCollection: MutableSet<MutableMap.MutableEntry<String, NmsTag>>
        get() = this.mapTo(ObjectOpenHashSet(this.size + 1, 0.99F)) { it.unwrapEntry }
}
