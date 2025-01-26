package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.event
import com.destroystokyo.paper.event.server.ServerTickStartEvent

@Init(
    stage = InitStage.POST_WORLD,
)
internal object EcsListener {

    @InitFun
    fun init() {

        // 开发日记 24/12/14
        // 关于实现 Blink 技能时遇到的问题: 如果在这里使用 ServerTickEndEvent,
        // 那么在 tick 函数内调用 Player#setVelocity 后玩家的加速度会被服务端的内部逻辑覆盖, 导致玩家会在瞬移到空中后自由落体.
        // 解决方法: 在这里使用 ServerTickStartEvent, 让服务端内部的逻辑在 tick 函数返回之后执行.
        event<ServerTickStartEvent> {
            WakameWorld.tick()
        }
    }

}