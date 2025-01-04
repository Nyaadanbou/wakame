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
    private val toId: Reference2IntMap<T> = Reference2IntOpenHashMap<T>(2048).apply { defaultReturnValue(-1) }
    private val byLocation: MutableMap<ResourceLocation, Holder.Reference<T>> = HashMap(2048)
    private val byKey: MutableMap<ResourceKey<T>, Holder.Reference<T>> = HashMap(2048)
    private val byValue: MutableMap<T, Holder.Reference<T>> = IdentityHashMap(2048)

    private var unregisteredIntrusiveHolders: MutableMap<ResourceLocation, Holder.Reference<T>>? = null

    override fun register(key: ResourceKey<T>, value: T): Holder.Reference<T> {
        if (this.byLocation.containsKey(key.location)) {
            throw Util.pauseInIde(IllegalStateException("Adding duplicate key '$key' to registry"))
        } else if (this.byValue.containsKey(value)) {
            throw Util.pauseInIde(IllegalStateException("Adding duplicate value '$value' to registry"))
        } else {
            // 如果已经存在 intrusiveReferenceHolder, 就不再创建新的 Holder,
            // 而是把函数传递进来的数据绑定到它上面, 并且将这个 Holder 储存在注册表当中.
            // 如果没有 intrusiveReferenceHolder, 那么就创建一个新的 Holder.
            val intrusiveReferenceHolder = this.unregisteredIntrusiveHolders?.remove(key.location)?.bindValue(value)
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

    override val isEmpty: Boolean
        get() = byKey.isEmpty()

    override fun getValue(key: ResourceKey<T>): T? = byKey[key]?.value
    override fun getValue(id: ResourceLocation): T? = byLocation[id]?.value

    override fun getResourceLocation(value: T): ResourceLocation? {
        return byValue[value]?.key?.location
    }

    override fun getResourceKey(value: T): ResourceKey<T>? {
        return byValue[value]?.key
    }

    override fun getAny(): Holder.Reference<T>? {
        return byId.first()
    }

    override fun getRandom(random: Random): Holder.Reference<T>? {
        return byId[random.nextInt(-1, byId.size)]
    }

    override val keySet: Set<ResourceLocation>
        get() = byLocation.keys
    override val entrySet: Set<Map.Entry<ResourceKey<T>, T>>
        get() = byKey.mapValues { (_, v) -> v.value }.entries
    override val registryKeySet: Set<ResourceKey<T>>
        get() = byKey.keys
    override val sequence: Sequence<T>
        get() = iterator().asSequence()

    override fun containsKey(key: ResourceKey<T>): Boolean = byKey.containsKey(key)
    override fun containsKey(id: ResourceLocation): Boolean = byLocation.containsKey(id)

    override fun createIntrusiveHolder(id: ResourceLocation): Holder.Reference<T> {
        val intrusiveReferenceHolder = Holder.Reference.createIntrusive(this, ResourceKey.create(this.key, id))
        if (unregisteredIntrusiveHolders == null) {
            unregisteredIntrusiveHolders = HashMap()
        }
        unregisteredIntrusiveHolders!![id] = intrusiveReferenceHolder
        return intrusiveReferenceHolder
    }

    override fun getHolder(rawId: Int): Holder.Reference<T>? = if (rawId >= 0 && rawId < byId.size) byId[rawId] else null
    override fun getHolder(key: ResourceKey<T>): Holder.Reference<T>? = byKey[key]
    override fun getHolder(id: ResourceLocation): Holder.Reference<T>? = byLocation[id]

    override fun wrapAsHolder(value: T): Holder<T> {
        return byValue[value] ?: Holder.direct(value)
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
) : MappedRegistry<T>(key), DefaultedRegistry<T> {
    override val defaultId: ResourceLocation = ResourceLocation.key(defaultId)
    private lateinit var defaultValue: Holder.Reference<T>

    override fun register(key: ResourceKey<T>, value: T): Holder.Reference<T> {
        val reference = super.register(key, value)
        if (this.defaultId == key.location) {
            this.defaultValue = reference
        }
        return reference
    }

    override fun getId(value: T): Int {
        val i = super.getId(value)
        return if (i == -1) super.getId(this.defaultValue.value) else i
    }

    override fun getResourceLocation(value: T): ResourceLocation {
        return super<MappedRegistry>.getResourceLocation(value) ?: this.defaultId
    }

    override fun getValue(id: ResourceLocation): T {
        return super<MappedRegistry>.getValue(id) ?: this.defaultValue.value
    }

    override fun getAny(): Holder.Reference<T>? {
        return this.defaultValue
    }

    override fun byId(index: Int): T {
        return super<MappedRegistry>.byId(index) ?: this.defaultValue.value
    }

    override fun getRandom(random: Random): Holder.Reference<T>? {
        return super.getRandom(random) ?: this.defaultValue
    }
}