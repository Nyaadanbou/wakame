package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [AttributeBundleFacadeRegistryConfigStorage::class]
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