package cc.mewcraft.wakame.util

import com.google.common.collect.BoundType
import com.google.common.collect.Range

/**
 * 将 Guava 的 Range 转换为 Kotlin 的 IntRange
 *
 * @return 转换后的 IntRange，如果无法转换则返回 null
 */
fun Range<Int>.toKotlinRange(): IntRange? {
    return if (hasLowerBound() && hasUpperBound()) {
        val start = if (lowerBoundType() == BoundType.CLOSED) lowerEndpoint() else lowerEndpoint() + 1
        val end = if (upperBoundType() == BoundType.CLOSED) upperEndpoint() else upperEndpoint() - 1

        start..end
    } else {
        // 没有定义边界
        null
    }
}