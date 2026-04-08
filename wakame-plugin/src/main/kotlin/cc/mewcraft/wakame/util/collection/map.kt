package cc.mewcraft.wakame.util.collection

fun <K, V : Any> Map<K?, V>.filterKeysNotNull(): Map<K, V> {
    return filterKeysNotNullTo(LinkedHashMap())
}

fun <K, V : Any, M : MutableMap<K, V>> Map<K?, V>.filterKeysNotNullTo(destination: M): M {
    return filterTo(destination as MutableMap<K?, V>) { (key, _) -> key != null } as M
}
