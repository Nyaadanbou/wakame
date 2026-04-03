package cc.mewcraft.wakame.util.math

import org.joml.Vector3f

object Vec3f {
    // 获取常用向量的一系列方法, 每次调用都返回新对象防止污染

    /**
     * 获取零向量.
     * 每次调用都返回新对象.
     */
    fun zero() = Vector3f(0f, 0f, 0f)

    /**
     * 获取单位向量.
     * 每次调用都返回新对象.
     */
    fun one() = Vector3f(1f, 1f, 1f)

    /**
     * 获取X轴正方向单位向量.
     * 每次调用都返回新对象.
     */
    fun unitX() = Vector3f(1f, 0f, 0f)

    /**
     * 获取Y轴正方向单位向量.
     * 每次调用都返回新对象.
     */
    fun unitY() = Vector3f(0f, 1f, 0f)

    /**
     * 获取Z轴正方向单位向量.
     * 每次调用都返回新对象.
     */
    fun unitZ() = Vector3f(0f, 0f, 1f)
}