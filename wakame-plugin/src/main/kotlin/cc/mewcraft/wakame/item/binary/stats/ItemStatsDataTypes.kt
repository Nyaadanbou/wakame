package cc.mewcraft.wakame.item.binary.stats

interface MapLikeItemStats<K, V> {
    fun get(key: K): V
    fun set(key: K, value: V)
}

interface NumericMapLikeItemStats<K, V : Number> : MapLikeItemStats<K, V> {
    fun increment(key: K, value: V)
}

interface SingleItemStats<V> {
    fun get(): V
    fun set(value: V)
}

interface NumericSingleItemStats<V : Number> : SingleItemStats<V> {
    val nbtPath: String
    fun increment(value: V)
}
