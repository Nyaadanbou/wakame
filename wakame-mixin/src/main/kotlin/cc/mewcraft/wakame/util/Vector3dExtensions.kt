package cc.mewcraft.wakame.util

import org.bukkit.Location
import org.bukkit.World
import org.joml.Vector3d

fun Location.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

fun Vector3d.toLocation(world: World? = null): Location {
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}

fun Vector3d.copy(): Vector3d {
    return clone() as Vector3d
}

infix fun Vector3d.sub(other: Vector3d): Vector3d {
    return this.copy().sub(other)
}

infix operator fun Vector3d.minus(other: Vector3d): Vector3d {
    return this.copy().sub(other)
}

infix fun Vector3d.add(other: Vector3d): Vector3d {
    return this.copy().add(other)
}

infix operator fun Vector3d.plus(other: Vector3d): Vector3d {
    return this.copy().add(other)
}

infix fun Vector3d.mul(scalar: Float): Vector3d {
    return this.copy().mul(scalar)
}

infix fun Vector3d.mul(other: Vector3d): Vector3d {
    return this.copy().mul(other)
}

infix fun Vector3d.cross(other: Vector3d): Vector3d {
    return this.copy().cross(other)
}