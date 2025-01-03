package cc.mewcraft.wakame.core

import com.mojang.datafixers.util.Either

interface Holder<T> {
    companion object {
        fun <T> direct(value: T): Holder<T> = Direct(value)
    }

    val value: T
    fun equals(id: ResourceLocation): Boolean
    fun equals(id: ResourceKey<T>): Boolean
    fun equals(entry: Holder<T>): Boolean
    fun unwrap(): Either<ResourceKey<T>, T>
    fun unwrapKey(): ResourceKey<T>?
    val kind: Kind
    fun canSerializeIn(owner: HolderOwner<T>): Boolean
    val registeredName: String
        get() = this.unwrapKey()?.location?.toString() ?: "[UNREGISTERED]"

    enum class Kind {
        DIRECT, REFERENCE
    }

    data class Direct<T>(override val value: T) : Holder<T> {
        override fun equals(id: ResourceLocation): Boolean = false
        override fun equals(id: ResourceKey<T>): Boolean = false
        override fun equals(entry: Holder<T>): Boolean = value == entry.value
        override fun unwrap(): Either<ResourceKey<T>, T> = Either.right(value)
        override fun unwrapKey(): ResourceKey<T>? = null
        override val kind: Kind = Kind.DIRECT
        override fun canSerializeIn(owner: HolderOwner<T>) = true
        override fun toString(): String = "Direct[$value]"
    }

    data class Reference<T>(
        private val owner: HolderOwner<T>,
        private val key: ResourceKey<T>,
        override val value: T,
    ): Holder<T> {
        override fun equals(id: ResourceLocation): Boolean = key.location == id
        override fun equals(id: ResourceKey<T>): Boolean = key == id
        override fun equals(entry: Holder<T>): Boolean = entry is Reference<*> && entry.key == key
        override fun unwrap(): Either<ResourceKey<T>, T> = Either.left(key)
        override fun unwrapKey(): ResourceKey<T>? = key
        override val kind: Kind = Kind.REFERENCE
        override fun canSerializeIn(owner: HolderOwner<T>) = this.owner.canSerialize(owner)
        override fun toString(): String = "Reference[$key=$value]"
    }

}