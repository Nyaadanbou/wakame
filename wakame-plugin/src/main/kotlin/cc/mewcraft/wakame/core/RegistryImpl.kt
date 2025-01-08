package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.Util
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.Reference2IntMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import java.util.IdentityHashMap
import kotlin.random.Random

open class MappedRegistry<T>(
    override val key: ResourceKey<out Registry<T>>,
) : WritableRegistry<T> {
    private val byId: ObjectList<Holder.Reference<T>> = ObjectArrayList(256)
    private val toId: Reference2IntMap<T> = Reference2IntOpenHashMap<T>(1024).apply { defaultReturnValue(-1) }
    private val byLocation: MutableMap<ResourceLocation, Holder.Reference<T>> = HashMap(1024)
    private val byKey: MutableMap<ResourceKey<T>, Holder.Reference<T>> = HashMap(1024)
    private val byValue: MutableMap<T, Holder.Reference<T>> = IdentityHashMap(1024)

    private var frozen: Boolean = false

    // 还未正式注册好的 intrusive holders.
    // 该映射在最终必须为空, 否则视为注册表错误.
    private val unregisteredIntrusiveHolders: MutableMap<ResourceLocation, Holder.Reference<T>> = HashMap()

    override fun update(key: ResourceKey<T>, value: T): Holder.Reference<T> {
        val holder = byKey[key]
        if (holder != null) {
            // 获取旧数据
            val oldValue = holder.value

            // 更新映射 toId
            val id = toId.getInt(oldValue)
            if (id != -1) {
                toId.removeInt(oldValue)
                toId.put(value, id)
            }

            // 更新映射 byValue
            byValue.remove(oldValue)
            byValue.put(value, holder)

            // 重新绑定的数据
            holder.bindValue(value)

            return holder
        } else {
            throw Util.pauseInIde(IllegalStateException("Trying to update unregistered key '$key' in registry ${this.key}"))
        }
    }

    override fun register(key: ResourceKey<T>, value: T): Holder.Reference<T> {
        if (frozen) {
            throw Util.pauseInIde(IllegalStateException("Adding new entry to frozen registry '${this.key}'"))
        } else if (this.byLocation.containsKey(key.location)) {
            throw Util.pauseInIde(IllegalStateException("Adding duplicate key '$key' to registry '${this.key}'"))
        } else if (this.byValue.containsKey(value)) {
            throw Util.pauseInIde(IllegalStateException("Adding duplicate value '$value' to registry '${this.key}'"))
        } else {
            // 如果已经存在 intrusiveReferenceHolder, 就不再创建新的 Holder,
            // 而是把函数传递进来的数据绑定到它上面, 并且将这个 Holder 储存在注册表当中.
            // 如果没有 intrusiveReferenceHolder, 那么就创建一个新的 Holder.
            val intrusiveReferenceHolder = this.unregisteredIntrusiveHolders.remove(key.location)?.bindValue(value)
            val resultantReferenceHolder = intrusiveReferenceHolder ?: this.byKey.getOrPut(key) { Holder.Reference.createStandalone(this, key, value) }

            this.byKey.put(key, resultantReferenceHolder)
            this.byLocation.put(key.location, resultantReferenceHolder)
            this.byValue.put(value, resultantReferenceHolder)
            val i = this.byId.size
            this.byId.add(resultantReferenceHolder)
            this.toId.put(value, i)
            return resultantReferenceHolder
        }
    }

    override fun resetRegistry() {
        this.byId.clear()
        this.toId.clear()
        this.byLocation.clear()
        this.byKey.clear()
        this.byValue.clear()
        this.unregisteredIntrusiveHolders.clear()
        this.frozen = false
    }

    override val isEmpty: Boolean
        get() = byKey.isEmpty()

    override fun freeze(): Registry<T> {
        if (frozen) {
            return this
        } else {
            validate().getOrThrow()
            frozen = true
            return this
        }
    }

    override fun getValue(key: ResourceKey<T>): T? = byKey[key]?.value
    override fun getValue(id: ResourceLocation): T? = byLocation[id]?.value

    override fun getResourceLocation(value: T): ResourceLocation? = byValue[value]?.key?.location
    override fun getResourceKey(value: T): ResourceKey<T>? = byValue[value]?.key

    override fun getAny(): Holder.Reference<T>? = byId.first()
    override fun getRandom(random: Random): Holder.Reference<T>? = byId.randomOrNull(random)

    override val keySet: Set<ResourceLocation>
        get() = byLocation.keys
    override val entrySet: Set<Map.Entry<ResourceKey<T>, T>>
        get() = byKey.mapValues { (_, ref) -> ref.value }.entries
    override val registryKeySet: Set<ResourceKey<T>>
        get() = byKey.keys
    override val holderSequence: Sequence<Holder.Reference<T>>
        get() = byId.asSequence()

    override fun containsKey(key: ResourceKey<T>): Boolean = byKey.containsKey(key)
    override fun containsKey(id: ResourceLocation): Boolean = byLocation.containsKey(id)

    override fun createIntrusiveHolder(id: ResourceLocation): Holder.Reference<T> {
        // 如果注册表已经存在 id 对应的数据, 那么直接返回已存在的实例.
        // 这种情况一般发生在 reload - Holder 已创建, 只需要更新数据.
        val existing = byLocation[id]
        if (existing != null) {
            return existing
        }

        if (frozen) {
            throw Util.pauseInIde(IllegalStateException("Trying to create intrusive holder for '$id' in frozen registry '${this.key}'"))
        }

        // 序列化可能会在该注册表未加载数据时, 多次使用同一个 id 创建 intrusive holder.
        // 这里必须保证, 即使多次以同一个 id 调用该函数, 也只会返回最开始创建的那一个实例.
        return unregisteredIntrusiveHolders.getOrPut(id) { Holder.Reference.createIntrusive(this, ResourceKey.create(this.key, id)) }
    }

    override fun validate(): Result<Registry<T>> = runCatching {
        if (!unregisteredIntrusiveHolders.isEmpty()) {
            // 检查是否有未注册的 intrusive reference holders.
            // 发生该错误的原因: 通常是引用该注册表的配置文件写错了 id,
            // 例如属性里写了 "fire" 元素但元素注册表里不存在 "fire"
            throw IllegalStateException("Found unregistered intrusive holders in registry '${this.key}': ${unregisteredIntrusiveHolders.keys.joinToString()}")
        } else if (!byId.all(Holder.Reference<T>::isBound)) {
            // 检查所有的 reference holders 都已经绑定了数据.
            // 发生该错误的原因: 暂时不太清楚, 等代码跑起来再看.
            throw IllegalStateException("Found unbound reference holders in registry '${this.key}': ${byId.filterNot(Holder.Reference<T>::isBound).map(Holder.Reference<T>::key).joinToString()}")
        } else {
            this
        }
    }

    override fun get(rawId: Int): Holder.Reference<T>? = if (rawId >= 0 && rawId < byId.size) byId[rawId] else null
    override fun get(key: ResourceKey<T>): Holder.Reference<T>? = byKey[key]
    override fun get(id: ResourceLocation): Holder.Reference<T>? = byLocation[id]

    override fun wrapAsHolder(value: T): Holder<T> {
        return byValue[value] ?: throw IllegalStateException("Trying to wrap unregistered value '$value' in registry '${this.key}'")
    }

    override fun getId(value: T): Int {
        return toId.getInt(value)
    }

    override fun byId(index: Int): T? {
        return byId.getOrNull(index)?.value
    }

    override fun size(): Int {
        return byId.size
    }

    override fun iterator(): Iterator<T> {
        return byId.asSequence().map(Holder<T>::value).iterator()
    }
}

open class DefaultedMappedRegistry<T>(
    defaultId: String,
    override val key: ResourceKey<out Registry<T>>,
) : MappedRegistry<T>(key), DefaultedWritableRegistry<T> {
    override val defaultId: ResourceLocation = ResourceLocations.withKoishNamespace(defaultId)
    override lateinit var defaultValue: Holder.Reference<T>

    override fun register(key: ResourceKey<T>, value: T): Holder.Reference<T> {
        val reference = super<MappedRegistry>.register(key, value)
        assignToDefaultValue(key, reference)
        return reference
    }

    private fun assignToDefaultValue(key: ResourceKey<T>, reference: Holder.Reference<T>) {
        if (this.defaultId == key.location) {
            this.defaultValue = reference
        }
    }

    override fun getId(value: T): Int {
        val i = super.getId(value)
        return if (i == -1) super.getId(this.defaultValue.value) else i
    }

    override fun getValueOrDefault(id: ResourceLocation): T {
        return super<MappedRegistry>.getValue(id) ?: this.defaultValue.value
    }

    override fun getValueOrDefault(key: ResourceKey<T>): T {
        return super<MappedRegistry>.getValue(key) ?: this.defaultValue.value
    }

    override fun getValueOrDefault(id: String): T {
        return super<MappedRegistry>.getValue(id) ?: this.defaultValue.value
    }

    override fun getResourceLocationOrDefault(value: T): ResourceLocation {
        return super<MappedRegistry>.getResourceLocation(value) ?: this.defaultId
    }

    override fun getResourceKeyOrDefault(value: T): ResourceKey<T> {
        return super<MappedRegistry>.getResourceKey(value) ?: ResourceKey.create(this.key, this.defaultId)
    }

    override fun byIdOrDefault(index: Int): T {
        return super<MappedRegistry>.byId(index) ?: this.defaultValue.value
    }

    override fun getAny(): Holder.Reference<T>? {
        return this.defaultValue
    }

    override fun getRandom(random: Random): Holder.Reference<T> {
        return super.getRandom(random) ?: this.defaultValue
    }

    override fun validate(): Result<Registry<T>> = super.validate().mapCatching { registry ->
        if (!this::defaultValue.isInitialized) {
            throw IllegalStateException("Default value not initialized in registry '${this.key}'")
        } else {
            this
        }
    }
}