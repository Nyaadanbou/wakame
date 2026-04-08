package cc.mewcraft.wakame.registry.entry

import com.mojang.datafixers.util.Either
import kotlin.random.Random

// 暂时没用的代码, 等之后引入了 Tag 系统后考虑
interface RegistryEntryList<T> : Iterable<RegistryEntry<T>> {
    fun sequence(): Sequence<RegistryEntry<T>>
    fun size(): Int
    fun isBound(): Boolean
    fun getStorage(): Either<Unit, List<RegistryEntry<T>>>
    fun getRandom(random: Random): RegistryEntry<T>?
    fun get(index: Int): RegistryEntry<T>
    fun contains(entry: RegistryEntry<T>): Boolean
    fun ownerEquals(owner: RegistryEntryOwner<T>): Boolean

    companion object {
        fun <T> empty(): RegistryEntryList<T> = Direct(emptyList())
        fun <T> direct(vararg entries: RegistryEntry<T>): Direct<T> = Direct(entries.toList())
        fun <T> direct(entries: List<RegistryEntry<T>>): Direct<T> = Direct(entries)
        fun <E, T> direct(mapper: (E) -> RegistryEntry<T>, vararg values: E): Direct<T> = direct(values.map(mapper))
        fun <E, T> direct(mapper: (E) -> RegistryEntry<T>, values: Collection<E>): Direct<T> = direct(values.map(mapper))
    }

    abstract class ListBacked<T> : RegistryEntryList<T> {
        protected abstract fun contents(): List<RegistryEntry<T>>

        override fun size(): Int = contents().size
        override fun iterator(): Iterator<RegistryEntry<T>> = contents().iterator()
        override fun sequence(): Sequence<RegistryEntry<T>> = contents().asSequence()
        override fun getRandom(random: Random): RegistryEntry<T>? = contents().randomOrNull(random)
        override fun get(index: Int): RegistryEntry<T> = contents()[index]
        override fun ownerEquals(owner: RegistryEntryOwner<T>): Boolean = true
    }

    class Direct<T>(
        private val contents: List<RegistryEntry<T>>,
    ) : ListBacked<T>() {
        private var contentsSet: Set<RegistryEntry<T>>? = null

        override fun contents(): List<RegistryEntry<T>> = contents
        override fun isBound(): Boolean = true
        override fun getStorage(): Either<Unit, List<RegistryEntry<T>>> = Either.right(contents)
        override fun contains(entry: RegistryEntry<T>): Boolean {
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
        private val owner: RegistryEntryOwner<T>,
        private val key: String,
    ) : ListBacked<T>() {
        private var contents: List<RegistryEntry<T>>? = null

        fun bind(entries: List<RegistryEntry<T>>) {
            contents = entries.toList()
        }

        fun key(): String = key

        override fun contents(): List<RegistryEntry<T>> {
            return contents ?: throw IllegalStateException("Trying to access unbound tag '$key' from registry $owner")
        }

        override fun isBound(): Boolean = contents != null
        override fun getStorage(): Either<Unit, List<RegistryEntry<T>>> = Either.left(Unit)
        override fun contains(entry: RegistryEntry<T>): Boolean = contents?.contains(entry) == true
        override fun toString(): String = "NamedSet($key)[$contents]"
        override fun ownerEquals(owner: RegistryEntryOwner<T>): Boolean = this.owner == owner
    }
}
