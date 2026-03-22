package cc.mewcraft.wakame.hook.impl.thebrewingproject

import cc.mewcraft.wakame.LOGGER
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import org.bukkit.Bukkit


object HookUtils {

    /**
     * 获取 [TheBrewingProjectApi] 实例并执行 [block].
     *
     * 如果 TBP 服务未注册, 则不执行.
     */
    fun withApi(block: (TheBrewingProjectApi) -> Unit) {
        val provider = Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi::class.java)
        if (provider != null) {
            block(provider.provider)
        } else {
            LOGGER.error("TheBrewingProject API is not initialized yet.")
        }
    }
}