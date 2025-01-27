package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryConfigStorage
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.RegistryConfigStorage

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryConfigStorage::class, // deps: 需要直接的数据
    ]
)
@Reload
object AbilityRegistryConfigStorage : RegistryConfigStorage {

    // TODO 有这么几类东西需要分别放入不同的注册表:
    // AbilityFactory
    // Ability
    // Trigger
    // PlayerAbility (仅在配置文件中, 不会被直接放进注册表?

    // TODO 由于已经在新分支上动了部分底层代码, 等这个 PR 合并后再写技能的

    @InitFun
    fun init() {

    }

    @ReloadFun
    fun reload() {

    }
}