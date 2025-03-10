package cc.mewcraft.wakame.hologram

import org.joml.Vector3f

/**
 * 展示实体动画的具体变换参数.
 */
internal data class AnimationData(
    val startInterpolation: Int,
    val interpolationDuration: Int,
    val translation: Vector3f,
    val scale: Vector3f,
) {
    constructor(
        parentData: AnimationData = DEFAULT,
        startInterpolation: Int?,
        interpolationDuration: Int?,
        translation: Vector3f?,
        scale: Vector3f?
    ) : this(
        startInterpolation ?: parentData.startInterpolation,
        interpolationDuration ?: parentData.interpolationDuration,
        translation ?: parentData.translation,
        scale ?: parentData.scale
    )

    companion object {
        val DEFAULT = AnimationData(0, 0, Vector3f(0f), Vector3f(1f))
    }
}
