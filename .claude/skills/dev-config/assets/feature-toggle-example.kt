// 完整示例: 简单功能开关
// 来源: dev-config SKILL.md §7

import cc.mewcraft.wakame.feature.FEATURE_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse

@Init(InitStage.POST_WORLD)
object ForceCommandLowercase : Listener {

    // 对应 configs/features.yml 中的 force_command_lowercase
    private val enabled by FEATURE_CONFIG.entryOrElse(false, "force_command_lowercase")

    @InitFun
    fun init() {
        if (enabled) registerEvents()
    }
}
