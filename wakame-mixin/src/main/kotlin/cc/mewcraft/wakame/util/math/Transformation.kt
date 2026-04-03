package cc.mewcraft.wakame.util.math

import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Transformation(
    val translation: Vector3f,
    val leftRotation: Quaternionf,
    val scale: Vector3f,
    val rightRotation: Quaternionf
) {
    /**
     * 构造一个单位变换.
     */
    constructor() : this(
        Vector3f(),
        Quaternionf(),
        Vector3f(1f),
        Quaternionf()
    )

    /**
     * 使用欧拉角构造变换.
     */
    constructor(
        translation: Vector3f,
        leftRotation: AxisAngle4f,
        scale: Vector3f,
        rightRotation: AxisAngle4f
    ) : this(
        translation,
        Quaternionf(leftRotation),
        scale,
        Quaternionf(rightRotation)
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transformation) return false

        return translation == other.translation &&
                leftRotation == other.leftRotation &&
                scale == other.scale &&
                rightRotation == other.rightRotation
    }

    override fun hashCode(): Int {
        var result = translation.hashCode()
        result = 31 * result + leftRotation.hashCode()
        result = 31 * result + scale.hashCode()
        result = 31 * result + rightRotation.hashCode()
        return result
    }

    override fun toString(): String {
        return "Transformation(" +
                "translation=$translation, " +
                "leftRotation=$leftRotation, " +
                "scale=$scale, " +
                "rightRotation=$rightRotation)"
    }

    companion object {
        /**
         * 获取单位变换.
         * 每次调用都返回新对象.
         */
        fun identity(): Transformation {
            return Transformation()
        }
    }
}