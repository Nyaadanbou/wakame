package cc.mewcraft.wakame.registry2.entry

import cc.mewcraft.wakame.registry2.ReactiveRegistryEntry
import cc.mewcraft.wakame.registry2.RegistryKey
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.asMinimalString2
import com.mojang.datafixers.util.Either
import org.jetbrains.annotations.ApiStatus
import xyz.xenondevs.commons.provider.MutableProvider
import xyz.xenondevs.commons.provider.mutableProvider
import xyz.xenondevs.commons.provider.provider
import kotlin.reflect.KProperty

/**
 * 用于持有数据的容器, 附加了专用于跟 [cc.mewcraft.wakame.registry2.Registry] 进行交互的函数.
 *
 * @see cc.mewcraft.wakame.registry2.Registry
 */
interface RegistryEntry<T> {
    companion object {
        fun <T> direct(value: T): Direct<T> = Direct(value)
    }

    /**
     * 返回容器的类型.
     */
    val type: Type

    /**
     * 返回容器里的数据.
     *
     * 通常在所有注册表已完全加载后的场景里使用.
     *
     * @throws IllegalStateException 如果数据还未绑定
     */
    val value: T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    /**
     * 检查该容器是否已经绑定了数据.
     */
    val hasValue: Boolean

    /**
     * 返回一个与该容器相关联的响应式对象 [ReactiveRegistryEntry].
     *
     * 通常在 *注册表还未完全加载但需要将数据进行转换并放在成员变量* 的场景里使用.
     */
    fun reactive(): ReactiveRegistryEntry<T>

    fun matchesKey(key: RegistryKey<T>): Boolean
    fun matchesId(id: Identifier): Boolean
    fun matches(entry: RegistryEntry<T>): Boolean

    /**
     * 返回容器的键名或数据.
     */
    fun getKeyOrValue(): Either<RegistryKey<T>, T>

    /**
     * 返回容器的键名.
     *
     * 该键名包含两种信息:
     * 1. 该容器 [RegistryEntry] 所在的注册表 [cc.mewcraft.wakame.registry2.Registry]
     * 2. 该容器 [RegistryEntry] 在 [cc.mewcraft.wakame.registry2.Registry] 中的位置
     *
     * 因此该键名可用于从任意注册表中确定一个数据.
     *
     * @see RegistryKey
     * @throws IllegalStateException 如果容器没有键名
     */
    fun getKey(): RegistryKey<T>?
    fun getKeyOrThrow(): RegistryKey<T> {
        return getKey() ?: throw IllegalStateException("No resource key associated with this entry")
    }

    /**
     * 返回该容器的注册名. (在已知一个注册表时) 该名字可用来唯一确定该数据.
     *
     * 当容器数据的命名空间为 [cc.mewcraft.wakame.util.KOISH_NAMESPACE] 时, 返回值部分不包含命名空间.
     * 否则返回完整的 `命名空间:路径` 形式. 在已知 [getIdAsString] 的情况下,
     * 逆操作 ([cc.mewcraft.wakame.util.Identifiers.of]) 也完全遵循该规则.
     *
     * 例如从 [Identifier] 转换为 [getIdAsString]:
     * - `"koish:ice"` -> `"ice"` (命名空间为 "koish" 将省略命名空间)
     * - `"kawaii:cute"` -> `"kawaii:cute"` (命名空间不是 "koish" 将保留完整路径)
     *
     * 例如从 [getIdAsString] 转换为 [Identifier]:
     * - `"ice"` -> `"koish:ice"` (省略命名空间时, 将自动设置为 "koish")
     * - `"kawaii:cute"` -> `"kawaii:cute"` (存在命名空间时, 将保留完整路径)
     */
    fun getIdAsString(): String {
        return this.getKey()?.value?.asMinimalString2() ?: "unregistered"
    }

    fun ownerEquals(owner: RegistryEntryOwner<T>): Boolean

    enum class Type {
        /**
         * 未与注册表相关联的数据.
         */
        DIRECT,

        /**
         * 与注册表相关联的数据.
         */
        REFERENCE
    }

    // RegistryEntry.Direct 封装的是没有与 Registry 相关联的数据
    class Direct<T>
    @ApiStatus.Internal
    constructor(override val value: T) : RegistryEntry<T> {
        override val type: Type
            get() = Type.DIRECT

        override val hasValue: Boolean
            get() = true

        private var reactive: ReactiveRegistryEntry<T>? = null

        @Synchronized
        override fun reactive(): ReactiveRegistryEntry<T> {
            return reactive ?: provider(value).also { reactive = it }
        }

        override fun matchesId(id: Identifier): Boolean = false
        override fun matchesKey(key: RegistryKey<T>): Boolean = false
        override fun matches(entry: RegistryEntry<T>): Boolean = this.value == entry.value
        override fun getKeyOrValue(): Either<RegistryKey<T>, T> = Either.right(this.value)
        override fun getKey(): RegistryKey<T>? = null
        override fun ownerEquals(owner: RegistryEntryOwner<T>) = true
        override fun toString(): String = "Direct[${this.value}]"
    }

    // RegistryEntry.Reference 的设计目的:
    // 允许外部从 Registry 用 Identifier 请求返回指定的数据, 并满足以下需求.
    //
    // 1) 请求时注册表都已经初始化完毕
    // 这种场景直接获取数据本身即可, 不需要先获取外层封装 RegistryEntry.Reference
    // 例如在整个系统全部初始化完毕后再获取注册表里的数据, 通常是处理游戏世界内发生的事件
    // 2) 请求时注册表还未初始化完毕
    // 这种情况一般出现在不同注册表之间相互依赖的场景里 (配置文件反序列化)
    //
    class Reference<T>
    private constructor(
        private val owner: RegistryEntryOwner<T>,
        val referenceType: Type, // 目前仅仅是标记, 无实际用途
        private val key: RegistryKey<T>,
        private var _value: T? = null,
    ) : RegistryEntry<T> {
        companion object {
            fun <T> standalone(owner: RegistryEntryOwner<T>, key: RegistryKey<T>, value: T): Reference<T> {
                return Reference(owner, Type.STANDALONE, key, value)
            }

            fun <T> intrusive(owner: RegistryEntryOwner<T>, key: RegistryKey<T>): Reference<T> {
                return Reference(owner, Type.INTRUSIVE, key)
            }
        }

        private var reactive: MutableProvider<T>? = null

        @Synchronized
        override fun reactive(): ReactiveRegistryEntry<T> {
            return reactive ?: mutableProvider(this::value).also { reactive = it }
        }

        override val hasValue: Boolean
            get() = _value != null

        override val value: T
            get() = _value ?: throw IllegalStateException("Trying to access unbound value '${this.key}' from registry '${this.owner}'")

        override val type: RegistryEntry.Type = RegistryEntry.Type.REFERENCE

        // 使用场景:
        // 1) WritableRegistry#add 注册一个新的数据
        // 1) WritableRegistry#add 为 intrusive registry entry 绑定数据
        // 2) 配置文件发生重载
        @ApiStatus.Internal
        fun setValue(value: T): Reference<T> {
            _value = value

            // 如果存在 reactive, 也要更新 reactive 链上的所有数据
            reactive?.set(value)

            return this // 返回 this, 方便链式调用
        }

        override fun matchesKey(key: RegistryKey<T>): Boolean = this.key === key
        override fun matchesId(id: Identifier): Boolean = this.key.value == id
        override fun matches(entry: RegistryEntry<T>): Boolean = this === entry || (entry is Reference<*> && this.key == entry.key)

        override fun hashCode(): Int {
            return this.key.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Reference<*>) return false
            return this.key === other.key
        }

        override fun getKeyOrValue(): Either<RegistryKey<T>, T> = Either.left(this.key)
        override fun getKey(): RegistryKey<T> = this.key
        override fun ownerEquals(owner: RegistryEntryOwner<T>) = this.owner.ownerEquals(owner)
        override fun toString(): String = "Reference[${this.key}=${this.value}]"

        enum class Type {
            /**
             * 由注册表自己使用 [cc.mewcraft.wakame.registry2.WritableRegistry.add] 创建的 [Reference].
             */
            STANDALONE,

            /**
             * 由外部调用 [cc.mewcraft.wakame.registry2.Registry.createEntry] 创建的 [Reference].
             */
            INTRUSIVE
        }
    }
}