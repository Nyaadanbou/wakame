package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.ecs.system.ManageBossBar
import cc.mewcraft.wakame.ecs.system.RenderParticle
import cc.mewcraft.wakame.ecs.system.UpdateEntityInfoBossBar
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object CommonFleksPatcher : FleksPatcher {

    @InitFun
    fun init() {
        addToRegistrySystem("manage_boss_bar") { ManageBossBar } // 显示/移除 boss bar
        addToRegistrySystem("render_particle") { RenderParticle } // 渲染粒子效果
        addToRegistrySystem("update_entity_info_boss_bar") { UpdateEntityInfoBossBar } // 更新各种关于 boss bar 的逻辑
    }
}