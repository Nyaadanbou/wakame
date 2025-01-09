package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.Util
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.Reference2IntMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import java.util.IdentityHashMap
import kotlin.random.Random

// FIXME rename to SimpleRegistry
// FIXME rename classes/functions according to Yarn mapping
open class SimpleRegistry<T>(
    override val key: RegistryKey<out Registry<T>>,
) : WritableRegistry<T> {
    private val rawIdToEntry: ObjectList<RegistryEntry.Reference<T>> = ObjectArrayList(256)
    private val entryToRawId: Reference2IntMap<T> = Reference2IntOpenHashMap<T>(1024).apply { defaultReturnValue(-1) }
    private val idToEntry: MutableMap<Identifier, RegistryEntry.Reference<T>> = HashMap(1024)
    private val keyToEntry: MutableMap<RegistryKey<T>, RegistryEntry.Reference<T>> = HashMap(1024)
    private val valueToEntry: MutableMap<T, RegistryEntry.Reference<T>> = IdentityHashMap(1024)

    private var frozen: Boolean = false

    // 还未正式注册好的 intrusive holders.
    // 该映射在最终必须为空, 否则视为注册表错误.
    private val intrusiveIdToEntry: MutableMap<Identifier, RegistryEntry.Reference<T>> = HashMap()

    override fun update(key: RegistryKey<T>, value: T): RegistryEntry.Reference<T> {
        val entry = keyToEntry[key]
        if (entry != null) {
            // 获取旧数据
            val oldValue = entry.value

            // 更新映射 toId
            val id = entryToRawId.getInt(oldValue)
            if (id != -1) {
                entryToRawId.removeInt(oldValue)
                entryToRawId.put(value, id)
            }

            // 更新映射 byValue
            valueToEntry.remove(oldValue)
            valueToEntry.put(value, entry)

            // 重新绑定的数据
            entry.setValue(value)

            return entry
        } else {
            throw Util.pauseInIde(IllegalStateException("Trying to update unregistered key '$key' in registry ${this.key}"))
        }
    }

    override fun add(key: RegistryKey<T>, value: T): RegistryEntry.Reference<T> {
        if (frozen) {
            throw Util.pauseInIde(IllegalStateException("Adding new entry to frozen registry '${this.key}'"))
        } else if (this.idToEntry.containsKey(key.value)) {
            throw Util.pauseInIde(IllegalStateException("Adding duplicate key '$key' to registry '${this.key}'"))
        } else if (this.valueToEntry.containsKey(value)) {
            throw Util.pauseInIde(IllegalStateException("Adding duplicate value '$value' to registry '${this.key}'"))
        } else {
            // 如果已经存在 intrusiveReferenceHolder, 就不再创建新的 Holder,
            // 而是把函数传递进来的数据绑定到它上面, 并且将这个 Holder 储存在注册表当中.
            // 如果没有 intrusiveReferenceHolder, 那么就创建一个新的 Holder.
            val intrusiveReferenceHolder = this.intrusiveIdToEntry.remove(key.value)?.setValue(value)
            val resultantReferenceEntry = intrusiveReferenceHolder ?: this.keyToEntry.getOrPut(key) { RegistryEntry.Reference.standalone(this, key, value) }

            this.keyToEntry.put(key, resultantReferenceEntry)
            this.idToEntry.put(key.value, resultantReferenceEntry)
            this.valueToEntry.put(value, resultantReferenceEntry)
            val i = this.rawIdToEntry.size
            this.rawIdToEntry.add(resultantReferenceEntry)
            this.entryToRawId.put(value, i)
            return resultantReferenceEntry
        }
    }

    override fun resetRegistry() {
        this.rawIdToEntry.clear()
        this.entryToRawId.clear()
        this.idToEntry.clear()
        this.keyToEntry.clear()
        this.valueToEntry.clear()
        this.intrusiveIdToEntry.clear()
        this.frozen = false
    }

    override val isEmpty: Boolean
        get() = keyToEntry.isEmpty()

    override fun freeze(): Registry<T> {
        if (frozen) {
            return this
        } else {
            validateEntries().getOrThrow()
            frozen = true
            return this
        }
    }

    override fun get(key: RegistryKey<T>): T? = keyToEntry[key]?.value
    override fun get(id: Identifier): T? = idToEntry[id]?.value

    override fun getId(value: T): Identifier? = valueToEntry[value]?.getKey()?.value
    override fun getKey(value: T): RegistryKey<T>? = valueToEntry[value]?.getKey()

    override fun getDefaultEntry(): RegistryEntry.Reference<T>? = rawIdToEntry.first()
    override fun getRandomEntry(random: Random): RegistryEntry.Reference<T>? = rawIdToEntry.randomOrNull(random)

    override val ids: Set<Identifier>
        get() = idToEntry.keys
    override val keys: Set<RegistryKey<T>>
        get() = keyToEntry.keys
    override val entrySet: Set<Map.Entry<RegistryKey<T>, T>>
        get() = keyToEntry.mapValues { (_, ref) -> ref.value }.entries
    override val entrySequence: Sequence<RegistryEntry.Reference<T>>
        get() = rawIdToEntry.asSequence()

    override fun containsKey(key: RegistryKey<T>): Boolean = keyToEntry.containsKey(key)
    override fun containsId(id: Identifier): Boolean = idToEntry.containsKey(id)

    override fun createEntry(id: Identifier): RegistryEntry.Reference<T> {
        // 如果注册表已经存在 id 对应的数据, 那么直接返回已存在的实例.
        // 这种情况一般发生在 reload - Holder 已创建, 只需要更新数据.
        val existing = idToEntry[id]
        if (existing != null) {
            return existing
        }

        if (frozen) {
            throw Util.pauseInIde(IllegalStateException("Trying to create intrusive holder for '$id' in frozen registry '${this.key}'"))
        }

        // 序列化可能会在该注册表未加载数据时, 多次使用同一个 id 创建 intrusive holder.
        // 这里必须保证, 即使多次以同一个 id 调用该函数, 也只会返回最开始创建的那一个实例.
        return intrusiveIdToEntry.getOrPut(id) { RegistryEntry.Reference.intrusive(this, RegistryKey.of(this.key, id)) }
    }

    override fun validateEntries(): Result<Registry<T>> = runCatching {
        if (!intrusiveIdToEntry.isEmpty()) {
            // 检查是否有未注册的 intrusive reference holders.
            // 发生该错误的原因: 通常是引用该注册表的配置文件写错了 id,
            // 例如属性里写了 "fire" 元素但元素注册表里不存在 "fire"
            throw IllegalStateException("Found unregistered intrusive holders in registry '${this.key}': ${intrusiveIdToEntry.keys.joinToString()}")
        } else if (!rawIdToEntry.all(RegistryEntry.Reference<T>::hasValue)) {
            // 检查所有的 reference holders 都已经绑定了数据.
            // 发生该错误的原因: 暂时不太清楚, 等代码跑起来再看.
            throw IllegalStateException("Found unbound reference holders in registry '${this.key}': ${rawIdToEntry.filterNot(RegistryEntry.Reference<T>::hasValue).map(RegistryEntry.Reference<T>::getKey).joinToString()}")
        } else {
            this
        }
    }

    override fun getEntry(rawId: Int): RegistryEntry.Reference<T>? = if (rawId >= 0 && rawId < rawIdToEntry.size) rawIdToEntry[rawId] else null
    override fun getEntry(key: RegistryKey<T>): RegistryEntry.Reference<T>? = keyToEntry[key]
    override fun getEntry(id: Identifier): RegistryEntry.Reference<T>? = idToEntry[id]

    override fun wrapAsHolder(value: T): RegistryEntry<T> {
        return valueToEntry[value] ?: throw IllegalStateException("Trying to wrap unregistered value '$value' in registry '${this.key}'")
    }

    override fun getRawId(value: T): Int {
        return entryToRawId.getInt(value)
    }

    override fun get(index: Int): T? {
        return rawIdToEntry.getOrNull(index)?.value
    }

    override fun size(): Int {
        return rawIdToEntry.size
    }

    override fun iterator(): Iterator<T> {
        return rawIdToEntry.asSequence().map(RegistryEntry<T>::value).iterator()
    }
}

open class SimpleDefaultedRegistry<T>(
    defaultId: String,
    override val key: RegistryKey<out Registry<T>>,
) : SimpleRegistry<T>(key), DefaultedWritableRegistry<T> {
    override val defaultId: Identifier = Identifiers.ofKoish(defaultId)
    private lateinit var defaultEntry: RegistryEntry.Reference<T>

    override fun add(key: RegistryKey<T>, value: T): RegistryEntry.Reference<T> {
        val reference = super<SimpleRegistry>.add(key, value)
        assignToDefaultEntry(key, reference)
        return reference
    }

    private fun assignToDefaultEntry(key: RegistryKey<T>, reference: RegistryEntry.Reference<T>) {
        if (this.defaultId == key.value) {
            this.defaultEntry = reference
        }
    }

    override fun getRawId(value: T): Int {
        val i = super.getRawId(value)
        return if (i == -1) super.getRawId(this.defaultEntry.value) else i
    }

    override fun getOrDefault(id: Identifier): T {
        return super<SimpleRegistry>.get(id) ?: this.defaultEntry.value
    }

    override fun getOrDefault(key: RegistryKey<T>): T {
        return super<SimpleRegistry>.get(key) ?: this.defaultEntry.value
    }

    override fun getOrDefault(id: String): T {
        return super<SimpleRegistry>.get(id) ?: this.defaultEntry.value
    }

    override fun getIdOrDefault(value: T): Identifier {
        return super<SimpleRegistry>.getId(value) ?: this.defaultId
    }

    override fun getKeyOrDefault(value: T): RegistryKey<T> {
        return super<SimpleRegistry>.getKey(value) ?: RegistryKey.of(this.key, this.defaultId)
    }

    override fun getOrDefault(rawId: Int): T {
        return super<SimpleRegistry>.get(rawId) ?: this.defaultEntry.value
    }

    override fun getDefaultEntry(): RegistryEntry.Reference<T> {
        return this.defaultEntry
    }

    override fun getRandomEntry(random: Random): RegistryEntry.Reference<T> {
        return super.getRandomEntry(random) ?: this.defaultEntry
    }

    override fun validateEntries(): Result<Registry<T>> = super.validateEntries().mapCatching { registry ->
        if (!this::defaultEntry.isInitialized) {
            throw IllegalStateException("Default value not initialized in registry '${this.key}'")
        } else {
            this
        }
    }
}