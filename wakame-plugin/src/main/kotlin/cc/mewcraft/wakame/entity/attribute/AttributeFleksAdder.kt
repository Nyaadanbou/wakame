package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.entity.attribute.system.ApplyAttributeEffects
import cc.mewcraft.wakame.entity.attribute.system.InitAttributeContainer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object AttributeFleksAdder : FleksAdder {
    @InitFun
    fun init() {
        addToRegistrySystem("apply_attribute_effects") { ApplyAttributeEffects } // 将物品上的属性效果应用到玩家
        addToRegistrySystem("init_attribute_container") { InitAttributeContainer } // 初始化玩家的属性容器
    }
}