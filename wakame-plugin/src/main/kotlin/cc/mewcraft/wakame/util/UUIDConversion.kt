package cc.mewcraft.wakame.util

import java.util.*

fun UUID.toIntArray(): IntArray {
    val mostSigBits = this.mostSignificantBits
    val leastSigBits = this.leastSignificantBits

    return intArrayOf(
        (mostSigBits shr 32).toInt(),
        mostSigBits.toInt(),
        (leastSigBits shr 32).toInt(),
        leastSigBits.toInt()
    )
}

fun IntArray.toUUID(): UUID {
    val mostSigBits = this[0].toLong() shl 32 or (this[1].toLong() and 0xFFFFFFFFL)
    val leastSigBits = this[2].toLong() shl 32 or (this[3].toLong() and 0xFFFFFFFFL)
    return UUID(mostSigBits, leastSigBits)
}
