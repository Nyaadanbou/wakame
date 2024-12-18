package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class Tags : EntityTags by entityTagOf() {
    /**
     * 代表此实体可以执行 [cc.mewcraft.wakame.ecs.Mechanic] 的 [cc.mewcraft.wakame.ecs.Mechanic.tick] 方法.
     */
    CAN_TICK,

    /**
     * 可以进行到下一个 [cc.mewcraft.wakame.ecs.data.StatePhase]
     */
    CAN_NEXT_STATE,

    /**
     * 代表此实体是一个临时实体, 会在下一个阶段被销毁.
     */
    DISPOSABLE
    ;
}