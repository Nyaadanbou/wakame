package cc.mewcraft.wakame.util

fun <K, V> Map<K, V>.getOrThrow(key: K): V =
    get(key) ?: throw NoSuchElementException("Can't find corresponding value for key $key")