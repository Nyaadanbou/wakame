package cc.mewcraft.wakame.util

object Predicates {
    fun <T> allOf(): (T) -> Boolean = { true }

    fun <T> allOf(predicate: (T) -> Boolean): (T) -> Boolean = predicate

    fun <T> allOf(predicates: List<(T) -> Boolean>): (T) -> Boolean = when (predicates.size) {
        0 -> allOf()
        1 -> allOf(predicates[0])
        else -> { t -> predicates.all { it(t) } }
    }

    fun <T> anyOf(): (T) -> Boolean = { false }

    fun <T> anyOf(predicate: (T) -> Boolean): (T) -> Boolean = predicate

    fun <T> anyOf(predicates: List<(T) -> Boolean>): (T) -> Boolean = when (predicates.size) {
        0 -> anyOf()
        1 -> anyOf(predicates[0])
        else -> { t -> predicates.any { it(t) } }
    }
}

