package cc.mewcraft.wakame.core

interface IndexedIterable<T> : Iterable<T> {
    companion object {
        const val DEFAULT = -1
    }

    fun get(index: Int): T?

    fun getOrThrow(index: Int): T {
        return get(index) ?: throw IllegalArgumentException("No value with id $index")
    }

    fun getRawId(value: T): Int

    fun getRawIdOrThrow(value: T): Int {
        val id = getRawId(value)
        if (id == DEFAULT) {
            throw IllegalArgumentException("Can't find id for '$value' in map $this")
        }
        return id
    }

    fun size(): Int
}