package cc.mewcraft.wakame.util

fun Number.toStableByte(): Byte =
    when (this) {
        is Byte -> this
        is Short -> toStableByte()
        is Int -> toStableByte()
        is Long -> toStableByte()
        is Float -> toStableByte()
        is Double -> toStableByte()
        else -> neverThrow()
    }

fun Number.toStableShort(): Short =
    when (this) {
        is Byte -> toStableShort()
        is Short -> this
        is Int -> toStableShort()
        is Long -> toStableShort()
        is Float -> toStableShort()
        is Double -> toStableShort()
        else -> neverThrow()
    }

fun Number.toStableInt(): Int =
    when (this) {
        is Byte -> toStableInt()
        is Short -> toStableInt()
        is Int -> this
        is Long -> toStableInt()
        is Float -> toStableInt()
        is Double -> toStableInt()
        else -> neverThrow()
    }

fun Number.toStableLong(): Long =
    when (this) {
        is Byte -> toStableLong()
        is Short -> toStableLong()
        is Int -> toStableLong()
        is Long -> this
        is Float -> toStableLong()
        is Double -> toStableLong()
        else -> neverThrow()
    }

fun Number.toStableFloat(): Float =
    when (this) {
        is Byte -> toStableFloat()
        is Short -> toStableFloat()
        is Int -> toStableFloat()
        is Long -> toStableFloat()
        is Float -> this
        is Double -> toStableFloat()
        else -> neverThrow()
    }

fun Number.toStableDouble(): Double =
    when (this) {
        is Byte -> toStableDouble()
        is Short -> toStableDouble()
        is Int -> toStableDouble()
        is Long -> toStableDouble()
        is Float -> toStableDouble()
        is Double -> this
        else -> neverThrow()
    }

private fun neverThrow(): Nothing = throw IllegalStateException()

////// Byte

fun Byte.toStableShort(): Short = this.toShort()
fun Byte.toStableInt(): Int = this.toInt()
fun Byte.toStableLong(): Long = this.toLong()
fun Byte.toStableFloat(): Float = this.toFloat()
fun Byte.toStableDouble(): Double = this.toDouble()

////// Short

fun Short.toStableByte(): Byte = this.coerceIn(Byte.MIN_VALUE.toShort(), Byte.MAX_VALUE.toShort()).toByte()
fun Short.toStableInt(): Int = this.toInt()
fun Short.toStableLong(): Long = this.toLong()
fun Short.toStableFloat(): Float = this.toFloat()
fun Short.toStableDouble(): Double = this.toDouble()

////// Int

fun Int.toStableByte(): Byte = this.coerceIn(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()).toByte()
fun Int.toStableShort(): Short = this.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
fun Int.toStableLong(): Long = this.toLong()
fun Int.toStableFloat(): Float = this.toFloat()
fun Int.toStableDouble(): Double = this.toDouble()

////// Long

fun Long.toStableByte(): Byte = this.coerceIn(Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong()).toByte()
fun Long.toStableShort(): Short = this.coerceIn(Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong()).toShort()
fun Long.toStableInt(): Int = this.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
fun Long.toStableFloat(): Float = this.coerceIn(-Float.MAX_VALUE.toLong(), Float.MAX_VALUE.toLong()).toFloat()
fun Long.toStableDouble(): Double = this.toDouble()

////// Float

fun Float.toStableByte(): Byte = this.coerceIn(Byte.MIN_VALUE.toFloat(), Byte.MAX_VALUE.toFloat()).toInt().toByte()
fun Float.toStableShort(): Short = this.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort()
fun Float.toStableInt(): Int = this.coerceIn(Int.MIN_VALUE.toFloat(), Int.MAX_VALUE.toFloat()).toInt()
fun Float.toStableLong(): Long = this.coerceIn(Long.MIN_VALUE.toFloat(), Long.MAX_VALUE.toFloat()).toLong()
fun Float.toStableDouble(): Double = this.toDouble()

////// Double

fun Double.toStableByte(): Byte = this.coerceIn(Byte.MIN_VALUE.toDouble(), Byte.MAX_VALUE.toDouble()).toInt().toByte()
fun Double.toStableShort(): Short = this.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
fun Double.toStableInt(): Int = this.coerceIn(Int.MIN_VALUE.toDouble(), Int.MAX_VALUE.toDouble()).toInt()
fun Double.toStableLong(): Long = this.coerceIn(Long.MIN_VALUE.toDouble(), Long.MAX_VALUE.toDouble()).toLong()
fun Double.toStableFloat(): Float = this.coerceIn(-Float.MAX_VALUE.toDouble(), Float.MAX_VALUE.toDouble()).toFloat()
