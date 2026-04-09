package cc.mewcraft.wakame.util.math

import kotlin.math.PI

/**
 * 单精度浮点型的圆周率.
 */
const val PI_FLOAT: Float = PI.toFloat()

/**
 * 把角度制转为弧度制.
 */
fun Float.toRadians(): Float = this * (PI_FLOAT / 180f)

/**
 * 把弧度制转为角度制.
 */
fun Float.toDegrees(): Float = this * (180f / PI_FLOAT)