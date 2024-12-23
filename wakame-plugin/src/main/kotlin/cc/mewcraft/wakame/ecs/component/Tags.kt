package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class Tags : EntityTags by entityTagOf() {
    /**
     * 代表此实体是一个临时实体, 会在下一个阶段被销毁. 此标记由外部系统添加. 不会被移除.
     */
    DISPOSABLE,

    /**
     * 代表此实体计划被移除, 此标记由具体系统添加, 由 [cc.mewcraft.wakame.ecs.system.InitSystem] 每 tick 开始时会移除.
     */
    READY_TO_REMOVE,

    /**
     * 代表此实体可进行下一个状态的转换. 此标记由外部系统添加, 会在下一个阶段被移除.
     */
    NEXT_STATE,
    ;
}