package cc.mewcraft.wakame.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class WatchedPrimitive<V>(
    initialValue: V,
    private val customPrefix: String? = null // 允许手动指定日志前缀
) : KoinComponent, ObservableProperty<V>(initialValue) {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    override fun afterChange(property: KProperty<*>, oldValue: V, newValue: V) {
        val loggerPrefix = customPrefix ?: "[${property.javaField?.declaringClass?.simpleName ?: "Local"}]"
        logger.info("$loggerPrefix ${property.name}: $oldValue -> $newValue")
    }
}

class WatchedCollection<E>(
    private val collection: MutableCollection<E>,
    private val customPrefix: String? = null
) : ReadOnlyProperty<Any?, MutableCollection<E>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableCollection<E> {
        return WatchedCollection0(property, collection, customPrefix)
    }
}

class WatchedList<E>(
    private val list: MutableList<E>,
    private val customPrefix: String? = null
) : ReadOnlyProperty<Any?, MutableList<E>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableList<E> {
        return WatchedList0(property, list, customPrefix)
    }
}

class WatchedSet<E>(
    private val set: MutableSet<E>,
    private val customPrefix: String? = null
): ReadOnlyProperty<Any?, MutableSet<E>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableSet<E> {
        return WatchedSet0(property, set, customPrefix)
    }
}

class WatchedMap<K, V>(
    private val map: MutableMap<K, V>,
    private val customPrefix: String? = null
) : ReadOnlyProperty<Any?, MutableMap<K, V>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableMap<K, V> {
        return WatchedMap0(property, map, customPrefix)
    }
}

private fun String.plusIfNot(it: Boolean, s: String): String = if (it.not()) this.plus(s) else this

private abstract class WatchedBase : KoinComponent {
    protected abstract val owner: KProperty<*>
    protected abstract val customPrefix: String?
    protected val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)
    protected val loggerPrefix: String by lazy(LazyThreadSafetyMode.NONE) { customPrefix ?: "[${owner.javaField?.declaringClass?.simpleName ?: "Local"}] ${owner.name}" }
}

private open class WatchedCollection0<E>(
    override val owner: KProperty<*>,
    private val collection: MutableCollection<E>,
    override val customPrefix: String? = null
) : WatchedBase(), MutableCollection<E> by collection {
    override fun clear() =
        collection.clear().also { logger.info("$loggerPrefix clear") }
    override fun add(element: E): Boolean =
        collection.add(element).also { logger.info("$loggerPrefix add: $element".plusIfNot(it, " (failed)")) }
    override fun addAll(elements: Collection<E>): Boolean =
        collection.addAll(elements).also { logger.info("$loggerPrefix addAll: ${elements.joinToString()}".plusIfNot(it, " (failed)")) }
    override fun remove(element: E): Boolean =
        collection.remove(element).also { logger.info("$loggerPrefix remove: $element".plusIfNot(it, " (failed)")) }
    override fun removeAll(elements: Collection<E>): Boolean =
        collection.removeAll(elements).also { logger.info("$loggerPrefix removeAll: ${elements.joinToString()}".plusIfNot(it, " (failed)")) }
}

private class WatchedList0<E>(
    override val owner: KProperty<*>,
    private val list: MutableList<E>,
    override val customPrefix: String? = null
) : WatchedBase(), MutableList<E> by list {
    private val collection: MutableCollection<E> = WatchedCollection0(owner, list, loggerPrefix)

    // Collection

    override fun clear() = collection.clear()
    override fun add(element: E): Boolean = collection.add(element)
    override fun addAll(elements: Collection<E>): Boolean = collection.addAll(elements)
    override fun remove(element: E): Boolean = collection.remove(element)
    override fun removeAll(elements: Collection<E>): Boolean = collection.removeAll(elements)

    // MutableList

    override fun add(index: Int, element: E) =
        list.add(index, element).also { logger.info("$loggerPrefix clear") }
    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        list.addAll(index, elements).also { logger.info("$loggerPrefix addAll: $index -> ${elements.joinToString()}") }
    override fun removeAt(index: Int): E =
        list.removeAt(index).also { logger.info("$loggerPrefix removeAt: $index") }
    override fun set(index: Int, element: E): E =
        list.set(index, element).also { logger.info("$loggerPrefix set: $index -> $element") }
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> =
        list.subList(fromIndex, toIndex).also { logger.info("$loggerPrefix subList: $fromIndex - $toIndex") }
}

private class WatchedSet0<E>(
    override val owner: KProperty<*>,
    private val set: MutableSet<E>,
    override val customPrefix: String? = null
) : WatchedBase(), MutableSet<E> by set {

    private val collection: MutableCollection<E> = WatchedCollection0(owner, set, customPrefix)

    // MutableSet

    /* the function signatures of MutableSet are the same as Collection */

    override fun clear() = collection.clear()
    override fun add(element: E): Boolean = collection.add(element)
    override fun addAll(elements: Collection<E>): Boolean =collection.addAll(elements)
    override fun remove(element: E): Boolean = collection.remove(element)
    override fun removeAll(elements: Collection<E>): Boolean = collection.removeAll(elements)
}

private class WatchedMap0<K, V>(
    override val owner: KProperty<*>,
    private val map: MutableMap<K, V>,
    override val customPrefix: String? = null
) : WatchedBase(), MutableMap<K, V> by map {
    override fun clear() =
        map.clear().also { logger.info("$loggerPrefix clear") }
    override fun put(key: K, value: V): V? =
        map.put(key, value).also { logger.info("$loggerPrefix put: $key -> $value") }
    override fun putAll(from: Map<out K, V>) =
        map.putAll(from).also { logger.info("$loggerPrefix putAll: ${from.entries.joinToString { (k, v) -> "$k -> $v" }}") }
    override fun remove(key: K): V? =
        map.remove(key).also { logger.info("$loggerPrefix remove: $key -> $it") }
    override fun remove(key: K, value: V): Boolean =
        map.remove(key, value).also { logger.info("$loggerPrefix remove: $key -> $value".plusIfNot(it, " (failed)")) }
}
