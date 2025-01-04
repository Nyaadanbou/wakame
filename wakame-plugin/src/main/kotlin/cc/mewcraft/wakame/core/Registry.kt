package cc.mewcraft.wakame.core

import com.mojang.serialization.Codec
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

    // 用于序列化, 例如注册表数据之间的依赖, NBT读写, Web系统 等只需要储存键名的地方
    val byNameCodec: Codec<T>
        get() = TODO()

    // 同上, 只不过返回的是 Holder<T>
    val holderByNameCodec: Codec<Holder<T>>
        get() = TODO()

    fun getValue(key: ResourceKey<T>): T?
    fun getValue(id: ResourceLocation): T?
    operator fun get(key: ResourceKey<T>): T? = getValue(key)
    operator fun get(id: ResourceLocation): T? = getValue(id)

    override fun <U> keys(ops: DynamicOps<U>): Sequence<U> = keySet.asSequence().map { ops.createString(it.toString()) }

    fun getResourceLocation(value: T): ResourceLocation?
    fun getResourceKey(value: T): ResourceKey<T>?

    // override fun getId(value: T?): Int // 当父接口的签名为 T 时, 子接口的签名不能为 T?. 反过来可以

    fun getAny(): Holder.Reference<T>?
    fun getOrThrow(key: ResourceKey<T>): T = getValue(key) ?: throw IllegalArgumentException("Missing key in ${this.key}: $key")
    fun getRandom(random: Random): Holder.Reference<T>?

    val keySet: Set<ResourceLocation>
    val entrySet: Set<Map.Entry<ResourceKey<T>, T>>
    val registryKeySet: Set<ResourceKey<T>>

    val sequence: Sequence<T>

    fun containsKey(key: ResourceKey<T>): Boolean
    fun containsKey(id: ResourceLocation): Boolean

    // 该函数是为了解决必须手动指定注册表之间所有依赖的问题, 设计大概是这样的:
    //
    // 外部可以使用该函数“声明”*现在或将来*需要一个键名为 id 的数据的 Holder.
    // 由于是 Holder 所以外部可以选择仅仅把引用存起来, 或者如果需要转换数据的话
    // 那么可以使用 Holder#reactive() 来进行一系列响应式的数据操作(lazy).
    //
    // 返回的 Holder 在一开始没有绑定的数据, 也就是说直接调用 Holder#value 会抛异常.
    // 当注册表里的数据全部加载完毕后, 此时 Holder 中的数据会被填充, 所有数据将正常返回.
    //
    // Registry#register 与 Registry#createIntrusiveHolder 的关系:
    //
    // 当整个 Koish 加载完毕后, 所有的注册表里的 Holder 也应该全部加载完毕了. 此时:
    // 有些 Holder 是由 Registry#register 创建的, 由这种方式创建的 Holder 一开始就绑定了数据.
    // 而有些 Holder 是由 Registry#createIntrusiveHolder 创建的, 由这种方式创建的 Holder 一开始没有绑定数据.
    // 没有绑定数据的 Holder 会在 Registry#register 执行时(通常是加载配置文件时)将数据绑定好.
    fun createIntrusiveHolder(id: ResourceLocation): Holder.Reference<T>

    fun getHolder(rawId: Int): Holder.Reference<T>?
    fun getHolder(key: ResourceKey<T>): Holder.Reference<T>?
    fun getHolder(id: ResourceLocation): Holder.Reference<T>?
    fun getHolderOrThrow(key: ResourceKey<T>): Holder.Reference<T> = getHolder(key) ?: throw IllegalStateException("Missing key in ${this.key}: $key")

    // 不知道有什么用, 暂时忽略
    fun wrapAsHolder(value: T): Holder<T>

    fun asHolderIdMap(): IdMap<Holder<T>> {
        return object : IdMap<Holder<T>> {
            override fun getId(value: Holder<T>): Int {
                return getId(value.value)
            }

            override fun byId(index: Int): Holder<T>? {
                return getHolder(index)
            }

            override fun size(): Int {
                return this@Registry.size()
            }

            override fun iterator(): Iterator<Holder<T>> {
                return sequence.map { wrapAsHolder(it) }.iterator()
            }
        }
    }

}

interface WritableRegistry<T> : Registry<T> {
    fun register(key: ResourceKey<T>, value: T): Holder.Reference<T>
    val isEmpty: Boolean
}

interface DefaultedRegistry<T> : Registry<T> {
    val defaultId: ResourceLocation
    override fun getValue(id: ResourceLocation): T
    override fun getResourceLocation(value: T): ResourceLocation
    override fun byId(index: Int): T
}
