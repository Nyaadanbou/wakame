package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTag

/**
 * 代表此实体计划被移除.
 * 为了更好的整理移除的代码, 使用 [cc.mewcraft.wakame.ecs.system.RemoveSystem] 来移除实体.
 */
data object Remove : EntityTag()