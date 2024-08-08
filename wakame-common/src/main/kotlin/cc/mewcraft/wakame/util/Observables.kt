package cc.mewcraft.wakame.util

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

object ObservableDelegates {
    fun <V> reference(source: V, prefix: String? = null): ReadWriteProperty<Any?, V> =
        ObservableReference(source, prefix)

    fun <E> collection(source: MutableCollection<E>, prefix: String? = null): ReadOnlyProperty<Any?, MutableCollection<E>> =
        ObservableCollection(source, prefix)

    fun <E> list(source: MutableList<E>, prefix: String? = null): ReadOnlyProperty<Any?, MutableList<E>> =
        ObservableList(source, prefix)

    fun <E> set(source: MutableSet<E>, prefix: String? = null): ReadOnlyProperty<Any?, MutableSet<E>> =
        ObservableSet(source, prefix)

    fun <K, V> map(source: MutableMap<K, V>, prefix: String? = null): ReadOnlyProperty<Any?, MutableMap<K, V>> =
        ObservableMap(source, prefix)
}

private object LoggerPrefix {
    /**
     * 使用属性的声明类名作为日志前缀.
     */
    fun declaringClass(property: KProperty<*>): String {
        return "[${property.javaField?.declaringClass?.simpleName ?: "Local"}]"
    }

    /**
     * 使用属性本身的名字作为日志前缀.
     */
    fun itself(property: KProperty<*>): String {
        return "[${property.name}]"
    }
}

private class ObservableReference<V>(
    source: V,
    var prefix: String? = null, // 允许手动指定日志前缀
) : ObservableProperty<V>(source) {
    override fun afterChange(property: KProperty<*>, oldValue: V, newValue: V) {
        if (prefix == null) {
            // fallback to default value
            prefix = LoggerPrefix.declaringClass(property)
        }
        println("$prefix Ref: `${property.name}`, Updated: $oldValue -> $newValue")
    }
}

private class ObservableCollection<E>(
    private val source: MutableCollection<E>,
    private val prefix: String? = null,
) : ReadOnlyProperty<Any?, MutableCollection<E>> {
    private var observable: ObservableCollection0<E>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableCollection<E> {
        var value: ObservableCollection0<E>? = observable
        if (value == null) {
            value = ObservableCollection0(property, source, prefix)
            observable = value
        }
        return value
    }
}

private class ObservableList<E>(
    private val source: MutableList<E>,
    private val prefix: String? = null,
) : ReadOnlyProperty<Any?, MutableList<E>> {
    private var observable: ObservableList0<E>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableList<E> {
        var value: ObservableList0<E>? = observable
        if (value == null) {
            value = ObservableList0(property, source, prefix)
            observable = value
        }
        return value
    }
}

private class ObservableSet<E>(
    private val source: MutableSet<E>,
    private val prefix: String? = null,
) : ReadOnlyProperty<Any?, MutableSet<E>> {
    private var observable: ObservableSet0<E>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableSet<E> {
        var observable: ObservableSet0<E>? = observable
        if (observable == null) {
            observable = ObservableSet0(property, source, prefix)
            observable = observable
        }
        return observable
    }
}

private class ObservableMap<K, V>(
    private val source: MutableMap<K, V>,
    private val prefix: String? = null,
) : ReadOnlyProperty<Any?, MutableMap<K, V>> {
    private var observable: ObservableMap0<K, V>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableMap<K, V> {
        var value: ObservableMap0<K, V>? = observable
        if (value == null) {
            value = ObservableMap0(property, source, prefix)
            observable = value
        }
        return value
    }
}

private fun failureMark(negate: Boolean): String =
    if (!negate) "(failed)" else ""

private class ObservableCollection0<E>(
    owner: KProperty<*>,
    private val source: MutableCollection<E>,
    prefix: String? = null,
) : MutableCollection<E> by source {
    private val prefix: String = prefix ?: "${LoggerPrefix.declaringClass(owner)} Collection: `${owner.name}`, Action:"

    override fun clear() =
        source.clear().also { println("$prefix `clear`") }

    override fun add(element: E): Boolean =
        source.add(element).also { println("$prefix `add`${failureMark(it)}, Params: $element") }

    override fun addAll(elements: Collection<E>): Boolean =
        source.addAll(elements).also { println("$prefix `addAll`${failureMark(it)}, Params: ${elements.joinToString()}") }

    override fun remove(element: E): Boolean =
        source.remove(element).also { println("$prefix `remove`${failureMark(it)}, Params: $element") }

    override fun removeAll(elements: Collection<E>): Boolean =
        source.removeAll(elements).also { println("$prefix `removeAll`${failureMark(it)}, Params: ${elements.joinToString()}") }
}

private class ObservableList0<E>(
    owner: KProperty<*>,
    private val source: MutableList<E>,
    prefix: String? = null,
) : MutableList<E> by source {
    private val prefix: String = prefix ?: "${LoggerPrefix.declaringClass(owner)} List: `${owner.name}`, Action:"

    // Collection

    override fun clear() =
        source.clear().also { println("$prefix `clear`") }

    override fun add(element: E): Boolean =
        source.add(element).also { println("$prefix `add`${failureMark(it)}, Params: $element") }

    override fun addAll(elements: Collection<E>): Boolean =
        source.addAll(elements).also { println("$prefix `addAll`${failureMark(it)}, Params: ${elements.joinToString()}") }

    override fun remove(element: E): Boolean =
        source.remove(element).also { println("$prefix `remove`${failureMark(it)}, Params: $element") }

    override fun removeAll(elements: Collection<E>): Boolean =
        source.removeAll(elements).also { println("$prefix `removeAll`${failureMark(it)}, Params: ${elements.joinToString()}") }

    // MutableList

    override fun add(index: Int, element: E) =
        source.add(index, element).also { println("$prefix `clear`") }

    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        source.addAll(index, elements).also { println("$prefix `addAll`${failureMark(it)}. Params: ($index, ${elements.joinToString()})") }

    override fun removeAt(index: Int): E =
        source.removeAt(index).also { println("$prefix `removeAt`. Params: $index") }

    override fun set(index: Int, element: E): E =
        source.set(index, element).also { println("$prefix `set`. Params: ($index, $element)") }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> =
        source.subList(fromIndex, toIndex).also { println("$prefix `subList`. Params: [$fromIndex, $toIndex)") }
}

private class ObservableSet0<E>(
    owner: KProperty<*>,
    private val source: MutableSet<E>,
    prefix: String? = null,
) : MutableSet<E> by source {
    private val prefix: String = prefix ?: "${LoggerPrefix.declaringClass(owner)} Set: `${owner.name}`, Action:"

    // MutableSet

    override fun clear() =
        source.clear().also { println("$prefix `clear`") }

    override fun add(element: E): Boolean =
        source.add(element).also { println("$prefix `add`${failureMark(it)}, Params: $element") }

    override fun addAll(elements: Collection<E>): Boolean =
        source.addAll(elements).also { println("$prefix `addAll`${failureMark(it)}, Params: ${elements.joinToString()}") }

    override fun remove(element: E): Boolean =
        source.remove(element).also { println("$prefix `remove`${failureMark(it)}, Params: $element") }

    override fun removeAll(elements: Collection<E>): Boolean =
        source.removeAll(elements).also { println("$prefix `removeAll`${failureMark(it)}, Params: ${elements.joinToString()}") }
}

private class ObservableMap0<K, V>(
    owner: KProperty<*>,
    private val source: MutableMap<K, V>,
    prefix: String? = null,
) : MutableMap<K, V> by source {
    private val prefix: String = prefix ?: "${LoggerPrefix.declaringClass(owner)} Map: `${owner.name}`, Action:"

    override fun clear() =
        source.clear().also { println("$prefix `clear`") }

    override fun put(key: K, value: V): V? =
        source.put(key, value).also { println("$prefix `put`${failureMark(it == null)}, Params: ($key, $value)") }

    override fun putAll(from: Map<out K, V>) =
        source.putAll(from).also { println("$prefix `putAll`, Params: ${from.entries.joinToString { (k, v) -> "($k, $v)" }}") }

    override fun remove(key: K): V? =
        source.remove(key).also { println("$prefix `remove`${failureMark(it == null)}, Params: ($key, $it)") }

    override fun remove(key: K, value: V): Boolean =
        source.remove(key, value).also { println("$prefix `remove`${failureMark(it)}, Params: ($key, $value)") }
}
