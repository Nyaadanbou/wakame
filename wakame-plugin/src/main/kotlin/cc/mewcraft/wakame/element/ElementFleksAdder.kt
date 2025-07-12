package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.element.system.InitElementStackContainer
import cc.mewcraft.wakame.element.system.TickElementStack
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object ElementFleksAdder : FleksAdder {
    @InitFun
    fun init() {
        addToRegistrySystem("init_element_stack_container") { InitElementStackContainer } // 初始化玩家的元素特效容器
        addToRegistrySystem("tick_element_stack") { TickElementStack } // 更新每 tick 的元素特效层数
    }
}