package cc.mewcraft.wakame.random

import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Represents a context that is both readable and writable by the whole
 * process of sample selection.
 *
 * @see BasicSelectionContext
 */
sealed interface SelectionContext {
    /**
     * All the [marks][Mark] that has been added to `this` context.
     */
    val marks: MutableCollection<Mark<*>>
}

/**
 * You can (and should) extend this class to create your own context.
 */
open class BasicSelectionContext : SelectionContext {
    override val marks: MutableCollection<Mark<*>> by WatchedCollection(ObjectArraySet(8))
}

private const val LOGGER_PREFIX = "[SelectionContextWatcher]"

/**
 * An implementation of [ObservableProperty] for watching the changes on
 * [SelectionContext].
 */
class WatchedPrimitive<V>(
    initialValue: V
) : KoinComponent, ObservableProperty<V>(initialValue) {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    override fun afterChange(property: KProperty<*>, oldValue: V, newValue: V) {
        logger.info("$LOGGER_PREFIX ${property.name} changed: $oldValue -> $newValue")
    }
}

class WatchedCollection<E>(
    private val collection: MutableCollection<E>
) : ReadOnlyProperty<Any?, MutableCollection<E>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableCollection<E> {
        return WatchedCollection0(property, collection)
    }
}

private class WatchedCollection0<E>(
    private val owner: KProperty<*>,
    private val collection: MutableCollection<E>
) : KoinComponent, MutableCollection<E> by collection {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    override fun clear() {
        logger.info("$LOGGER_PREFIX ${owner.name} clear")
        collection.clear()
    }

    override fun add(element: E): Boolean {
        logger.info("$LOGGER_PREFIX ${owner.name} add: $element")
        return collection.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        logger.info("$LOGGER_PREFIX ${owner.name} addAll: ${elements.joinToString()}")
        return collection.addAll(elements)
    }

    override fun remove(element: E): Boolean {
        logger.info("$LOGGER_PREFIX ${owner.name} remove: ${element}")
        return collection.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        logger.info("$LOGGER_PREFIX ${owner.name} removeAll: ${elements.joinToString()}")
        return collection.removeAll(elements)
    }
}

class WatchedMap<K, V, >(
    private val map: MutableMap<K, V>
) : ReadOnlyProperty<Any?, MutableMap<K, V>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableMap<K, V> {
        return WatchedMap0(property, map)
    }
}

private class WatchedMap0<K, V>(
    private val owner: KProperty<*>,
    private val map: MutableMap<K, V>
) : KoinComponent, MutableMap<K, V> by map {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    override fun clear() {
        logger.info("$LOGGER_PREFIX ${owner.name} clear")
        map.clear()
    }

    override fun put(key: K, value: V): V? {
        logger.info("$LOGGER_PREFIX ${owner.name} put: $key -> $value")
        return map.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        logger.info("$LOGGER_PREFIX ${owner.name} putAll: ${from.entries.joinToString { (k, v) -> "$k -> $v" }}")
        return map.putAll(from)
    }

    override fun remove(key: K): V? {
        logger.info("$LOGGER_PREFIX ${owner.name} remove: $key")
        return map.remove(key)
    }

    override fun remove(key: K, value: V): Boolean {
        logger.info("$LOGGER_PREFIX ${owner.name} remove: $key -> $value")
        return map.remove(key, value)
    }
}