package cc.mewcraft.wakame.ecs

/**
 * 用于在 ECS 内启动 [com.github.quillraven.fleks.Family] 的引导器接口.
 */
fun interface FamiliesBootstrapper {
    fun bootstrap(): Families
}