package cc.mewcraft.wakame.core

import com.mojang.datafixers.util.Either
import org.jetbrains.annotations.ApiStatus
import xyz.xenondevs.commons.provider.immutable.provider

/**
 * 用于持有数据的容器, 附加了专用于跟 [Registry] 进行交互的函数.
 *
 * @see Registry
 */
interface Holder<T> {
    companion object {
        fun <T> direct(value: T): Holder<T> = Direct(value)
    }

    /**
     * 是否已经绑定了数据.
     */
    val isBound: Boolean

    /**
     * 获取容器的键名.
     *
     * @throws IllegalStateException 如果容器没有键名.
     */
    val key: ResourceKey<T>

    /**
     * 获取容器里的数据.
     *
     * 通常在所有注册表已完全加载后的场景里使用.
     *
     * @throws IllegalStateException 如果数据还未绑定.
     */
    val value: T

    /**
     * 获取该容器的类型.
     */
    val kind: Kind

    /**
     * 将数据转换成响应式对象.
     *
     * 通常在 *注册表还未完全加载但需要将数据进行转换并储存在成员变量* 的场景里使用.
     */
    fun reactive(): ReactiveHolder<T>

    fun equals(id: ResourceLocation): Boolean
    fun equals(key: ResourceKey<T>): Boolean
    fun equals(entry: Holder<T>): Boolean

    fun unwrap(): Either<ResourceKey<T>, T>
    fun unwrapKey(): ResourceKey<T>?
    val registeredName: String
        get() = this.unwrapKey()?.location?.toString() ?: "unregistered"

    fun canSerializeIn(owner: HolderOwner<T>): Boolean

    enum class Kind {
        DIRECT, REFERENCE
    }

    // Holder.Direct 封装的是没有与 Registry 相关联的数据
    class Direct<T>
    @ApiStatus.Internal
    constructor(override val value: T) : Holder<T> {
        override val isBound: Boolean
            get() = true

        override val key: ResourceKey<T>
            get() = throw IllegalStateException("Trying to access key of direct holder with value: '$this.value'")

        private var reactive: ReactiveHolder<T>? = null

        @Synchronized
        override fun reactive(): ReactiveHolder<T> {
            return reactive ?: provider(value).also { reactive = it }
        }

        override fun equals(id: ResourceLocation): Boolean = false
        override fun equals(key: ResourceKey<T>): Boolean = false
        override fun equals(entry: Holder<T>): Boolean = this.value == entry.value
        override fun unwrap(): Either<ResourceKey<T>, T> = Either.right(this.value)
        override fun unwrapKey(): ResourceKey<T>? = null
        override val kind: Kind = Kind.DIRECT
        override fun canSerializeIn(owner: HolderOwner<T>) = true
        override fun toString(): String = "Direct[${this.value}]"
    }

    // Holder.Reference 的设计目的:
    // 允许外部从 Registry 用 ResourceLocation 请求返回指定的数据, 并满足以下需求.
    //
    // 1) 请求时注册表都已经初始化完毕
    // 这种场景直接获取数据本身即可, 不需要先获取外层封装 Holder.Ref
    // 例如在整个系统全部初始化完毕后再获取注册表里的数据, 通常是处理游戏世界内发生的事件
    // 2) 请求时注册表还未初始化完毕
    // 这种情况一般出现在不同注册表之间相互依赖的场景里
    //
    class Reference<T>
    private constructor(
        private val owner: HolderOwner<T>,
        val type: Type, // 目前仅仅是标记, 无实际用途
        override val key: ResourceKey<T>,
        private var _value: T? = null,
    ) : Holder<T> {
        companion object {
            fun <T> createStandalone(owner: HolderOwner<T>, key: ResourceKey<T>, value: T): Reference<T> {
                return Reference(owner, Type.STANDALONE, key, value)
            }

            fun <T> createIntrusive(owner: HolderOwner<T>, key: ResourceKey<T>): Reference<T> {
                return Reference(owner, Type.INTRUSIVE, key)
            }
        }

        private var reactive: ReactiveHolder<T>? = null

        @Synchronized
        override fun reactive(): ReactiveHolder<T> {
            return reactive ?: provider(this::value).also { reactive = it }
        }

        override val isBound: Boolean
            get() = _value != null

        override val value: T
            get() = _value ?: throw IllegalStateException("Trying to access unbound value '${this.key}' from registry ${this.owner}")

        override val kind: Kind = Kind.REFERENCE

        // 使用场景:
        // 1) WritableRegistry#register 注册一个新的数据
        // 1) WritableRegistry#register 为 intrusive holder 绑定数据
        // 2) 配置文件发生重载
        @ApiStatus.Internal
        fun bindValue(value: T): Reference<T> {
            _value = value

            // 如果存在 reactive, 也要更新 reactive 链上的所有数据
            if (reactive != null) {
                reactive!!.update()
            }

            return this // 返回 this, 方便链式调用
        }

        override fun equals(id: ResourceLocation): Boolean = this.key.location == id
        override fun equals(key: ResourceKey<T>): Boolean = this.key == key
        override fun equals(entry: Holder<T>): Boolean = entry is Reference<*> && this.key == entry.key

        override fun hashCode(): Int {
            return this.key.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Reference<*>) return false
            return this.key == other.key
        }

        override fun unwrap(): Either<ResourceKey<T>, T> = Either.left(this.key)
        override fun unwrapKey(): ResourceKey<T> = this.key
        override fun canSerializeIn(owner: HolderOwner<T>) = this.owner.canSerializeIn(owner)
        override fun toString(): String = "Reference[${this.key}=${this.value}]"

        enum class Type {
            STANDALONE, INTRUSIVE
        }
    }
}