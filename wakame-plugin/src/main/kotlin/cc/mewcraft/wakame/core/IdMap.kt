package cc.mewcraft.wakame.core

interface IdMap<T> : Iterable<T> {
    companion object {
        const val DEFAULT = -1
    }

    fun getId(value: T): Int

    fun byId(index: Int): T?

    fun byIdOrThrow(index: Int): T {
        return byId(index) ?: throw IllegalArgumentException("No value with id $index")
    }

    fun getIdOrThrow(value: T): Int {
        val id = getId(value)
        if (id == DEFAULT) {
            throw IllegalArgumentException("Can't find id for '$value' in map $this")
        }
        return id
    }

    fun size(): Int
}