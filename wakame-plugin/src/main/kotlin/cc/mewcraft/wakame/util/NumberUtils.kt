package cc.mewcraft.wakame.util

import kotlin.random.Random

fun IntRange.random(randomNegative: Boolean = true): Int {
    return if (this.isEmpty()) throw NoSuchElementException("Cannot get random value from empty range.")
    else Random.nextInt(this.first, this.last + 1) * if (randomNegative) Random.nextInt(0, 2) * 2 - 1 else 1
}

fun LongRange.random(randomNegative: Boolean = true): Long {
    return if (this.isEmpty()) throw NoSuchElementException("Cannot get random value from empty range.")
    else Random.nextLong(this.first, this.last + 1) * if (randomNegative) Random.nextInt(0, 2) * 2 - 1 else 1
}

fun ClosedFloatingPointRange<Double>.random(randomNegative: Boolean = true): Double {
    return if (this.isEmpty()) throw NoSuchElementException("Cannot get random value from empty range.")
    else Random.nextDouble(this.start, this.endInclusive) * if (randomNegative) Random.nextInt(0, 2) * 2 - 1 else 1
}