package cc.mewcraft.wakame.core

import com.mojang.datafixers.util.Either
import kotlin.random.Random

// 暂时没用的代码, 等之后引入了 Tag 系统后考虑
interface HolderSet<T> : Iterable<Holder<T>> {
    fun sequence(): Sequence<Holder<T>>
    fun size(): Int
    fun isBound(): Boolean
    fun unwrap(): Either<Unit, List<Holder<T>>>
    fun getRandomElement(random: Random): Holder<T>?
    fun get(index: Int): Holder<T>
    fun contains(entry: Holder<T>): Boolean
    fun canSerializeIn(owner: HolderOwner<T>): Boolean

    companion object {
        fun <T> empty(): HolderSet<T> = Direct(emptyList())
        fun <T> direct(vararg entries: Holder<T>): Direct<T> = Direct(entries.toList())
        fun <T> direct(entries: List<Holder<T>>): Direct<T> = Direct(entries)
        fun <E, T> direct(mapper: (E) -> Holder<T>, vararg values: E): Direct<T> = direct(values.map(mapper))
        fun <E, T> direct(mapper: (E) -> Holder<T>, values: Collection<E>): Direct<T> = direct(values.map(mapper))
    }

    abstract class ListBacked<T> : HolderSet<T> {
        protected abstract fun contents(): List<Holder<T>>

        override fun size(): Int = contents().size
        override fun iterator(): Iterator<Holder<T>> = contents().iterator()
        override fun sequence(): Sequence<Holder<T>> = contents().asSequence()
        override fun getRandomElement(random: Random): Holder<T>? = contents().randomOrNull(random)
        override fun get(index: Int): Holder<T> = contents()[index]
        override fun canSerializeIn(owner: HolderOwner<T>): Boolean = true
    }

    class Direct<T>(
        private val contents: List<Holder<T>>,
    ) : ListBacked<T>() {
        private var contentsSet: Set<Holder<T>>? = null

        override fun contents(): List<Holder<T>> = contents
        override fun isBound(): Boolean = true
        override fun unwrap(): Either<Unit, List<Holder<T>>> = Either.right(contents)
        override fun contains(entry: Holder<T>): Boolean {
            if (contentsSet == null) {
                contentsSet = contents.toSet()
            }
            return contentsSet?.contains(entry) == true
        }

        override fun toString(): String = "DirectSet[$contents]"
        override fun equals(other: Any?): Boolean {
            return this === other || (other is Direct<*> && contents == other.contents)
        }

        override fun hashCode(): Int = contents.hashCode()
    }

    class Named<T>(
        private val owner: HolderOwner<T>,
        private val key: String,
    ) : ListBacked<T>() {
        private var contents: List<Holder<T>>? = null

        fun bind(entries: List<Holder<T>>) {
            contents = entries.toList()
        }

        fun key(): String = key

        override fun contents(): List<Holder<T>> {
            return contents ?: throw IllegalStateException("Trying to access unbound tag '$key' from registry $owner")
        }

        override fun isBound(): Boolean = contents != null
        override fun unwrap(): Either<Unit, List<Holder<T>>> = Either.left(Unit)
        override fun contains(entry: Holder<T>): Boolean = contents?.contains(entry) == true
        override fun toString(): String = "NamedSet($key)[$contents]"
        override fun canSerializeIn(owner: HolderOwner<T>): Boolean = this.owner == owner
    }
}
