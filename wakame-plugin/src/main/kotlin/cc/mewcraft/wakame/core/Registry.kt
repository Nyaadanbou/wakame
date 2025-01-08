package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.util.DataResults
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlin.random.Random

// 重构 Registry 以解决以下问题:
// 1) 序列化代码存在高度重复, 旧代码不易维护, 新功能不易添加
// 2) 当注册表A里的数据构建依赖注册表B里的数据时, 必须指定严格的初始化顺序
// 3) 当一个对象的成员需要来自注册表里的数据时, 必须手动编写懒加载机制, 或严格控制加载的顺序

/**
 * 代表一个注册表.
 *
 * @see Holder
 * @see ResourceKey
 * @see ResourceLocation
 */
interface Registry<T> : HolderOwner<T>, Keyable, IdMap<T> {
    val key: ResourceKey<out Registry<T>>

    fun asWritable(): WritableRegistry<T> = this as WritableRegistry<T>
    fun asDefaulted(): DefaultedRegistry<T> = this as DefaultedRegistry<T>
    fun asDefaultedWritable(): DefaultedWritableRegistry<T> = this as DefaultedWritableRegistry<T>

    // 用于序列化, 例如注册表数据之间的依赖, NBT读写, Web系统 等只需要储存键名的地方
    val valueByNameCodec: Codec<T>
        get() = referenceHolderCodec.flatComapMap(
            { holder -> holder.value },
            { value -> this.safeCastToReference(this.wrapAsHolder(value)) }
        )

    // 同上, 只不过返回的是 Holder<T>
    val holderByNameCodec: Codec<Holder<T>>
        get() = referenceHolderCodec.flatComapMap(
            { holder -> holder },
            { holder -> this.safeCastToReference(holder) }
        )

    private val referenceHolderCodec: Codec<Holder.Reference<T>>
        get() = ResourceLocations.CODEC.comapFlatMap(
            // 如果该 id 已经存在一个 holder, 那么直接返回已存在的, 否则就尝试创建一个 intrusive holder
            { id -> this[id]?.let(DataResult<Holder.Reference<T>>::success) ?: DataResult.success(this.createIntrusiveHolder(id)) },
            { holder -> holder.unwrapKey().location }
        )

    private fun safeCastToReference(holder: Holder<T>): DataResult<Holder.Reference<T>> {
        return DataResults.wrap(holder as? Holder.Reference<T>) { "Unregistered holder in registry ${this.key}: $holder" }
    }

    /**
     * 初始化后必须调用一次.
     */
    fun freeze(): Registry<T>

    fun getValue(key: ResourceKey<T>): T?
    fun getValue(id: ResourceLocation): T?
    fun getValue(id: String): T? = getValue(ResourceLocations.withKoishNamespace(id))
    fun getValueOrThrow(key: ResourceKey<T>): T = getValue(key) ?: throw IllegalArgumentException("Missing key in ${this.key}: $key")
    fun getValueOrThrow(id: ResourceLocation): T = getValue(id) ?: throw IllegalArgumentException("Missing id in ${this.key}: $id")
    fun getValueOrThrow(id: String): T = getValueOrThrow(ResourceLocations.withKoishNamespace(id))

    override fun <U> keys(ops: DynamicOps<U>): Sequence<U> = keySet.asSequence().map { ops.createString(it.toString()) }

    fun getResourceLocation(value: T): ResourceLocation?
    fun getResourceKey(value: T): ResourceKey<T>?

    // override fun getId(value: T?): Int // 当父接口的签名为 T 时, 子接口的签名不能为 T?. 反过来可以

    fun getAny(): Holder.Reference<T>?
    fun getRandom(random: Random): Holder.Reference<T>?

    val keySet: Set<ResourceLocation>
    val entrySet: Set<Map.Entry<ResourceKey<T>, T>>
    val registryKeySet: Set<ResourceKey<T>>

    val holderSequence: Sequence<Holder.Reference<T>>
    val valueSequence: Sequence<T>
        get() = holderSequence.map(Holder.Reference<T>::value)

    fun containsKey(key: ResourceKey<T>): Boolean
    fun containsKey(id: ResourceLocation): Boolean
    fun containsKey(id: String): Boolean = containsKey(ResourceLocations.withKoishNamespace(id))

    /**
     * 创建一个侵入式的 [Holder.Reference] 实例并返回.
     *
     * ### 注意事项
     * 实现上并不会始终创建一个侵入式的 [Holder.Reference] 实例.
     * 如果注册表里已经存在对应的数据, 那么将直接返回已存在的 [Holder].
     * 多次以相同的 [id] 调用该函数将返回同一个 [Holder.Reference] 实例.
     *
     * ### 设计哲学
     * 该函数是为了解决必须手动指定注册表之间所有依赖的问题, 设计大概是这样的:
     *
     * 外部可以使用该函数“声明”*现在或将来*需要一个键名为 [id] 的数据的 [Holder].
     * 由于是 [Holder] 所以外部可以选择仅仅把引用存起来, 或者如果需要转换数据的话
     * 那么可以使用 [Holder.reactive] 来进行一系列响应式的数据操作(lazy).
     *
     * 返回的 [Holder] 在一开始没有绑定的数据, 也就是说直接调用 [Holder.value] 会抛异常.
     * 当注册表里的数据全部加载完毕后, 此时 [Holder] 中的数据会被填充, 所有数据将正常返回.
     *
     * [WritableRegistry.register] 与 [Registry.createIntrusiveHolder] 的关系:
     *
     * 当整个 Koish 加载完毕后, 所有的注册表里的 [Holder] 也应该全部加载完毕了. 此时:
     * 有些 [Holder] 是由 [WritableRegistry.register] 创建的, 由这种方式创建的 [Holder] 一开始就绑定了数据.
     * 而有些 [Holder] 是由 [Registry.createIntrusiveHolder] 创建的, 由这种方式创建的 [Holder] 一开始没有绑定数据.
     * 没有绑定数据的 [Holder] 会在 [WritableRegistry.register] 执行时(通常是加载配置文件时)将数据绑定好.
     */
    fun createIntrusiveHolder(id: ResourceLocation): Holder.Reference<T>
    fun createIntrusiveHolder(id: String): Holder.Reference<T> = createIntrusiveHolder(ResourceLocations.withKoishNamespace(id))

    /**
     * 验证注册表的数据是否正确.
     *
     * 该函数应该在所有 [Registry] 数据都已经加载完毕后调用.
     * 必要的时机应该是: 首次初始化时, 以及每次重新加载数据时.
     */
    fun validate(): Result<Registry<T>>

    operator fun get(rawId: Int): Holder.Reference<T>?
    operator fun get(key: ResourceKey<T>): Holder.Reference<T>?
    operator fun get(id: ResourceLocation): Holder.Reference<T>?
    operator fun get(id: String): Holder.Reference<T>? = get(ResourceLocations.withKoishNamespace(id))
    fun getOrThrow(rawId: Int): Holder.Reference<T> = get(rawId) ?: throw IllegalStateException("Missing raw id in registry ${this.key}: $rawId")
    fun getOrThrow(key: ResourceKey<T>): Holder.Reference<T> = get(key) ?: throw IllegalStateException("Missing key in registry ${this.key}: $key")
    fun getOrThrow(id: ResourceLocation): Holder.Reference<T> = get(id) ?: throw IllegalStateException("Missing id in registry ${this.key}: $id")
    fun getOrThrow(id: String): Holder.Reference<T> = getOrThrow(ResourceLocations.withKoishNamespace(id))

    fun wrapAsHolder(value: T): Holder<T>

    fun asHolderIdMap(): IdMap<Holder<T>> {
        return object : IdMap<Holder<T>> {
            override fun getId(value: Holder<T>): Int {
                return this@Registry.getId(value.value)
            }

            override fun byId(index: Int): Holder<T>? {
                return this@Registry[index]
            }

            override fun size(): Int {
                return this@Registry.size()
            }

            override fun iterator(): Iterator<Holder<T>> {
                return this@Registry.holderSequence.iterator()
            }
        }
    }
}

interface WritableRegistry<T> : Registry<T> {
    fun update(key: ResourceKey<T>, value: T): Holder.Reference<T>
    fun update(id: ResourceLocation, value: T): Holder.Reference<T> = update(ResourceKey.create(this.key, id), value)
    fun update(id: String, value: T): Holder.Reference<T> = update(ResourceLocations.withKoishNamespace(id), value)

    fun register(key: ResourceKey<T>, value: T): Holder.Reference<T>
    fun register(id: ResourceLocation, value: T): Holder.Reference<T> = register(ResourceKey.create(this.key, id), value)
    fun register(id: String, value: T): Holder.Reference<T> = register(ResourceLocations.withKoishNamespace(id), value)

    // fun registerOrUpdate(key: ResourceKey<T>, value: T): Holder.Reference<T> {
    //     return if (containsKey(key)) update(key, value) else register(key, value)
    // }
    //
    // fun registerOrUpdate(id: ResourceLocation, value: T): Holder.Reference<T> = registerOrUpdate(ResourceKey.create(this.key, id), value)
    // fun registerOrUpdate(id: String, value: T): Holder.Reference<T> = registerOrUpdate(ResourceLocations.withKoishNamespace(id), value)

    /**
     * 仅在注册表初始化之前调用.
     */
    // 因为单元测试会重复执行初始化, 继而出现添加重复数据的情况, 所以暴露了这么一个函数.
    fun resetRegistry()

    val isEmpty: Boolean
}

interface DefaultedRegistry<T> : Registry<T> {
    val defaultId: ResourceLocation
    val defaultValue: Holder.Reference<T>

    fun getValueOrDefault(key: ResourceKey<T>): T
    fun getValueOrDefault(id: ResourceLocation): T
    fun getValueOrDefault(id: String): T
    fun getResourceLocationOrDefault(value: T): ResourceLocation
    fun getResourceKeyOrDefault(value: T): ResourceKey<T>
    fun byIdOrDefault(index: Int): T
}

interface DefaultedWritableRegistry<T> : DefaultedRegistry<T>, WritableRegistry<T>