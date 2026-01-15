package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.KoishPlugin
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.BungeeCordProxyServerSwitcher
import cc.mewcraft.wakame.util.ProxyServerSwitcher
import org.bukkit.Bukkit


@Init(InitStage.POST_WORLD)
internal object BungeeCordPluginMessagingChannelInitializer {

    @InitFun
    fun init() {
        Bukkit.getServer().messenger.registerOutgoingPluginChannel(KoishPlugin, "BungeeCord")
    }
}

@Init(InitStage.POST_WORLD)
internal object ProxyServerSwitcherInitializer {

    @InitFun
    fun init() {
        ProxyServerSwitcher.setImplementation(BungeeCordProxyServerSwitcher())
    }
}