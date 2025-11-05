package cc.mewcraft.wakame.hook.impl.mythicdungeons

import cc.mewcraft.wakame.integration.Hook

// TODO 先留个入口, 如果以后要写兼容可以直接继续

/**
 * 目前该钩子用于:
 *
 * 1. 当 MythicDungeons 和 CarbonChat 同时存在时, 注册基于 CarbonChat 实现的 Party 系统
 */
@Hook(plugins = ["MythicDungeons", "CarbonChat"])
object CarbonChatCompat {

}
