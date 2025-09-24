package cc.mewcraft.wakame.hook.impl.thebrewingproject

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.registry.BuiltInRegistries

@Hook(plugins = ["TheBrewingProject"])
object TheBrewingProjectHook {

    init {
        // 使 Koish 可以识别 TheBrewingProject 物品
        registerKoishItemIntegration()

        // 关于使 TheBrewingProject 可以识别 Koish 物品的实现决策:
        // 必须在 TBP 那边注册, 否则服务端启动 TBP 首次加载配方时会失败
    }

    private fun registerKoishItemIntegration() {
        BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL.add("thebrewingproject", TheBrewingProjectItemRefHandler)
    }
}