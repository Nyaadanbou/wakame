package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.registry2.entry.RegistryEntryOwner
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.Keyable
import cc.mewcraft.wakame.util.collection.IndexedIterable
import cc.mewcraft.wakame.util.dfu.DataResults
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlin.random.Random

// 重构 Registry 以解决以下问题:
// 1) 序列化代码存在高度重复, 旧代码不易维护, 新功能不易添加
// 2) 当注册表A里的数据构建依赖注册表B里的数据时, 必须指定严格的初始化顺序
// 3) 当一个对象的成员需要来自注册表里的数据时, 必须手动编写懒加载机制, 或严格控制加载的顺序

/**
 * 注册表.
 *
 * @see RegistryEntry
 * @see RegistryKey
 * @see Identifier
 */
interface Registry<T> : RegistryEntryOwner<T>, Keyable, IndexedIterable<T> {

    /**
     * 方便函数. 用于注册一个数据到注册表里, 同时返回注册的数据.
     */
    companion object {
        // 注册数据 //

        fun <T> register(registry: Registry<in T>, id: String, entry: T): T {
            return register(registry, Identifiers.of(id), entry)
        }

        fun <V, T : V> register(registry: Registry<V>, id: Identifier, entry: T): T {
            return register(registry, RegistryKey.of(registry.key, id), entry)
        }

        fun <V, T : V> register(registry: Registry<V>, key: RegistryKey<V>, entry: T): T {
            (registry as WritableRegistry<V>).add(key, entry as V)
            return entry
        }

        fun <T> registerReference(registry: Registry<T>, key: RegistryKey<T>, entry: T): RegistryEntry.Reference<T> {
            return (registry as WritableRegistry<T>).add(key, entry)
        }

        fun <T> registerReference(registry: Registry<T>, id: Identifier, entry: T): RegistryEntry.Reference<T> {
            return registerReference(registry, RegistryKey.of(registry.key, id), entry)
        }

        // 创建注册表 //

        /**
         * 创建一个注册表, 位于根命名空间下.
         */
        fun <T> of(id: String): SimpleRegistry<T> {
            return SimpleRegistry(RegistryKey.ofRegistry(Identifiers.of(id)))
        }
    }

    val key: RegistryKey<out Registry<T>>

    // 用于序列化, 例如注册表数据之间的依赖, NBT读写, Web系统 等只需要储存键名的地方
    val valueByNameCodec: Codec<T>
        get() = referenceEntryCodec.flatComapMap(
            { entry -> entry.unwrap() },
            { value -> this.safeCastToReference(this.wrapAsEntry(value)) }
        )

    // 同上, 只不过返回的是 RegistryEntry<T>
    val entryCodec: Codec<RegistryEntry<T>>
        get() = referenceEntryCodec.flatComapMap(
            { entry -> entry },
            { entry -> this.safeCastToReference(entry) }
        )

    private val referenceEntryCodec: Codec<RegistryEntry.Reference<T>>
        get() = Identifiers.CODEC.comapFlatMap(
            // 如果该 id 已经存在一个 entry, 那么直接返回已存在的, 否则就尝试创建一个 intrusive entry
            { id -> this.getEntry(id)?.let { DataResult.success(it) } ?: DataResult.success(this.createEntry(id)) },
            { entry -> entry.getKey().value }
        )

    private fun safeCastToReference(entry: RegistryEntry<T>): DataResult<RegistryEntry.Reference<T>> {
        return DataResults.wrap(entry as? RegistryEntry.Reference<T>) { "Unregistered entry in registry ${this.key}: $entry" }
    }

    /**
     * 初始化后必须调用一次.
     */
    fun freeze(): Registry<T>

    operator fun get(key: RegistryKey<T>): T?
    operator fun get(id: Identifier): T?
    operator fun get(id: String): T? = get(Identifiers.of(id))
    fun getOrThrow(key: RegistryKey<T>): T = get(key) ?: throw IllegalArgumentException("Missing key in ${this.key}: $key")
    fun getOrThrow(id: Identifier): T = get(id) ?: throw IllegalArgumentException("Missing id in ${this.key}: $id")
    fun getOrThrow(id: String): T = getOrThrow(Identifiers.of(id))

    override fun <U> keys(ops: DynamicOps<U>): Sequence<U> = ids.asSequence().map { ops.createString(it.toString()) }

    fun getId(value: T): Identifier?
    fun getKey(value: T): RegistryKey<T>?

    // override fun getId(value: T?): Int // 当父接口的签名为 T 时, 子接口的签名不能为 T?. 反过来可以

    fun getDefaultEntry(): RegistryEntry.Reference<T>?
    fun getRandomEntry(random: Random): RegistryEntry.Reference<T>?

    val ids: Set<Identifier>
    val keys: Set<RegistryKey<T>>
    val entrySet: Set<Map.Entry<RegistryKey<T>, T>>

    val entrySequence: Sequence<RegistryEntry.Reference<T>>
    val valueSequence: Sequence<T>
        get() = entrySequence.map(RegistryEntry.Reference<T>::unwrap)

    fun containsKey(key: RegistryKey<T>): Boolean
    fun containsId(id: Identifier): Boolean
    fun containsId(id: String): Boolean = containsId(Identifiers.of(id))

    /**
     * 创建一个侵入式的 [RegistryEntry.Reference] 实例并返回.
     *
     * ### 注意事项
     * 实现上并不会**始终**创建一个侵入式的 [RegistryEntry.Reference] 实例.
     * 如果注册表里已经存在对应的数据, 那么将直接返回已存在的 [RegistryEntry].
     * 多次以相同 [id] 调用该函数将返回同一个 [RegistryEntry.Reference] 实例.
     *
     * ### 设计哲学
     * 该函数为了解决: 必须手动指定注册表之间所有依赖的问题,
     * 即使注册表之间依赖的东西仅仅是一个数据的键名 ([id]).
     *
     * 设计大概是这样的:
     *
     * 外部可以使用该函数“声明”*现在或将来*需要一个键名为 [id] 的数据的 [RegistryEntry].
     * 由于是 [RegistryEntry] 所以外部可以选择仅仅把引用存起来, 或者如果需要转换数据的话
     * 那么可以使用 [RegistryEntry.reactive] 来进行一系列响应式的数据操作(lazy).
     *
     * 返回的 [RegistryEntry] 在一开始没有绑定的数据, 也就是说直接调用 [RegistryEntry.unwrap] 会抛异常.
     * 当注册表里的数据全部加载完毕后, 此时 [RegistryEntry] 中的数据会被填充, 所有数据将正常返回.
     *
     * [WritableRegistry.add] 与 [Registry.createEntry] 的关系:
     *
     * 当整个 Koish 加载完毕后, 所有的注册表里的 [RegistryEntry] 也应该全部加载完毕了. 此时:
     * 有些 [RegistryEntry] 是由 [WritableRegistry.add] 创建的, 由这种方式创建的 [RegistryEntry] 一开始就绑定了数据.
     * 而有些 [RegistryEntry] 是由 [Registry.createEntry] 创建的, 由这种方式创建的 [RegistryEntry] 一开始没有绑定数据.
     * 没有绑定数据的 [RegistryEntry] 会在 [WritableRegistry.add] 执行时(通常是加载配置文件时)将数据绑定好.
     */
    fun createEntry(id: Identifier): RegistryEntry.Reference<T>
    fun createEntry(id: String): RegistryEntry.Reference<T> = createEntry(Identifiers.of(id))

    /**
     * 验证注册表的数据是否正确.
     *
     * 该函数应该在所有 [Registry] 数据都已经加载完毕后调用.
     * 必要的时机应该是: 首次初始化时, 以及每次重新加载数据时.
     */
    fun validateEntries(): Result<Registry<T>>

    fun getEntry(rawId: Int): RegistryEntry.Reference<T>?
    fun getEntry(key: RegistryKey<T>): RegistryEntry.Reference<T>?
    fun getEntry(id: Identifier): RegistryEntry.Reference<T>?
    fun getEntry(id: String): RegistryEntry.Reference<T>? = getEntry(Identifiers.of(id))
    fun getEntryOrThrow(rawId: Int): RegistryEntry.Reference<T> = getEntry(rawId) ?: throw IllegalStateException("Missing raw id in registry ${this.key}: $rawId")
    fun getEntryOrThrow(key: RegistryKey<T>): RegistryEntry.Reference<T> = getEntry(key) ?: throw IllegalStateException("Missing key in registry ${this.key}: $key")
    fun getEntryOrThrow(id: Identifier): RegistryEntry.Reference<T> = getEntry(id) ?: throw IllegalStateException("Missing id in registry ${this.key}: $id")
    fun getEntryOrThrow(id: String): RegistryEntry.Reference<T> = getEntryOrThrow(Identifiers.of(id))

    fun wrapAsEntry(value: T): RegistryEntry<T>

    fun getIndexedEntries(): IndexedIterable<RegistryEntry<T>> {
        return object : IndexedIterable<RegistryEntry<T>> {
            override fun getRawId(value: RegistryEntry<T>): Int {
                return this@Registry.getRawId(value.unwrap())
            }

            override fun get(index: Int): RegistryEntry<T>? {
                return this@Registry.getEntry(index)
            }

            override fun size(): Int {
                return this@Registry.size()
            }

            override fun iterator(): Iterator<RegistryEntry<T>> {
                return this@Registry.entrySequence.iterator()
            }
        }
    }
}

interface WritableRegistry<T> : Registry<T> {
    fun update(key: RegistryKey<T>, value: T): RegistryEntry.Reference<T>
    fun update(id: Identifier, value: T): RegistryEntry.Reference<T> = update(RegistryKey.of(this.key, id), value)
    fun update(id: String, value: T): RegistryEntry.Reference<T> = update(Identifiers.of(id), value)

    fun upsert(key: RegistryKey<T>, value: T): RegistryEntry.Reference<T> {
        return if (containsKey(key)) update(key, value) else add(key, value)
    }
    fun upsert(id: Identifier, value: T): RegistryEntry.Reference<T> = upsert(RegistryKey.of(this.key, id), value)
    fun upsert(id: String, value: T): RegistryEntry.Reference<T> = upsert(Identifiers.of(id), value)

    fun add(key: RegistryKey<T>, value: T): RegistryEntry.Reference<T>
    fun add(id: Identifier, value: T): RegistryEntry.Reference<T> = add(RegistryKey.of(this.key, id), value)
    fun add(id: String, value: T): RegistryEntry.Reference<T> = add(Identifiers.of(id), value)

    /**
     * 仅在注册表初始化之前调用.
     */
    // 因为单元测试会重复执行初始化, 继而出现添加重复数据的情况, 所以暴露了这么一个函数.
    fun resetRegistry()

    val isEmpty: Boolean
}

interface DynamicRegistry<T> : WritableRegistry<T> {
    fun remove(key: RegistryKey<T>): RegistryEntry.Reference<T>
    fun remove(id: Identifier): RegistryEntry.Reference<T> = remove(RegistryKey.of(this.key, id))
    fun remove(id: String): RegistryEntry.Reference<T> = remove(Identifiers.of(id))
}

interface DefaultedRegistry<T> : Registry<T> {
    val defaultId: Identifier

    fun getOrDefault(key: RegistryKey<T>): T
    fun getOrDefault(id: Identifier): T
    fun getOrDefault(id: String): T
    fun getOrDefault(rawId: Int): T
    fun getIdOrDefault(value: T): Identifier
    fun getKeyOrDefault(value: T): RegistryKey<T>

    override fun getDefaultEntry(): RegistryEntry.Reference<T>
}

interface FuzzyRegistry<T> : Registry<T> {
    /**
     * 忽略命名空间进行模糊查找.
     *
     * @param id 模糊查询的 id, 不包含命名空间
     * @return 匹配的数据列表
     */
    fun getFuzzy(id: String): List<T>
}

// TODO 排列组合的接口越到后面越难实现, 装饰器模式能派上用场吗? 配合 kotlin 的 by 关键字是不是能简化代码?

interface WritableDefaultedRegistry<T> : WritableRegistry<T>, DefaultedRegistry<T>

interface WritableFuzzyRegistry<T> : WritableRegistry<T>, FuzzyRegistry<T>

interface WritableDefaultedFuzzyRegistry<T> : WritableRegistry<T>, DefaultedRegistry<T>, FuzzyRegistry<T>
