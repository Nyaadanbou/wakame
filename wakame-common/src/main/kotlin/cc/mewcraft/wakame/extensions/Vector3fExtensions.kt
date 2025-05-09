package cc.mewcraft.wakame.extensions

import org.bukkit.Location
import org.bukkit.World
import org.joml.Vector3f
import kotlin.math.abs

fun Location.toVector3f(): Vector3f {
    return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
}

fun Vector3f.toLocation(world: World? = null): Location {
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}

fun Vector3f.copy(): Vector3f {
    return clone() as Vector3f
}

infix fun Vector3f.sub(other: Vector3f): Vector3f {
    return this.copy().sub(other)
}

infix operator fun Vector3f.minus(other: Vector3f): Vector3f {
    return this.copy().sub(other)
}

infix fun Vector3f.add(other: Vector3f): Vector3f {
    return this.copy().add(other)
}

infix operator fun Vector3f.plus(other: Vector3f): Vector3f {
    return this.copy().add(other)
}

infix fun Vector3f.mul(scalar: Float): Vector3f {
    return this.copy().mul(scalar)
}

infix fun Vector3f.mul(other: Vector3f): Vector3f {
    return this.copy().mul(other)
}

infix fun Vector3f.dot(other: Vector3f): Float {
    return this.dot(other)
}

infix fun Vector3f.cross(other: Vector3f): Vector3f {
    return this.copy().cross(other)
}

fun Vector3f.isUnit(epsilon: Float = 1e-6f): Boolean {
    return abs(length() - 1f) < epsilon
}

fun Vector3f.isOrthogonalTo(other: Vector3f, epsilon: Float = 1e-6f): Boolean {
    return abs(this dot other) < epsilon
}

