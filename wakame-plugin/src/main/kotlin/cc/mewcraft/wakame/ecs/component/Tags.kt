package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class Tags : EntityTags by entityTagOf() {
    /**
     * 代表此实体可以执行 [cc.mewcraft.wakame.ecs.Mechanic] 的 [cc.mewcraft.wakame.ecs.Mechanic.tick] 方法.
     * 此标记由 [cc.mewcraft.wakame.ecs.system.InitSystem] 每 tick 添加.
     * 并在具体系统逻辑中移除 (如不满足 tick 条件).
     */
    CAN_TICK,

    /**
     * 可以进行到下一个 [cc.mewcraft.wakame.ecs.data.StatePhase].
     * 此标记由 [cc.mewcraft.wakame.ecs.system.StatePhaseSystem] 添加.
     * 并在 [cc.mewcraft.wakame.ecs.system.StatePhaseSystem] 中移除.
     */
    CAN_NEXT_STATE,

    /**
     * 代表此实体是一个临时实体, 会在下一个阶段被销毁. 此标记由外部系统添加. 不会被移除.
     */
    DISPOSABLE,

    /**
     * 代表此实体计划被移除, 此标记由具体系统添加, 此标记由 [cc.mewcraft.wakame.ecs.system.InitSystem] 每 tick 开始时会移除.
     */
    READY_TO_REMOVE,
    ;
}