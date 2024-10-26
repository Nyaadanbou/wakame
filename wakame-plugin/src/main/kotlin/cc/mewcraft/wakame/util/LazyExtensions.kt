package cc.mewcraft.wakame.util

fun <T, R> Lazy<T>.map(transform: (T) -> R): Lazy<R> = lazy { transform(this.value) }