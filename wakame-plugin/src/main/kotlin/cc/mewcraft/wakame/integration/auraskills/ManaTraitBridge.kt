package cc.mewcraft.wakame.integration.auraskills

import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import org.bukkit.entity.Player

interface ManaTraitBridge : OnlineUserTicker {
    companion object Impl : ManaTraitBridge {
        private var implementation: ManaTraitBridge = object : ManaTraitBridge {}

        fun setImplementation(impl: ManaTraitBridge) {
            implementation = impl
        }

        override fun onTickUser(user: User, player: Player) {
            implementation.onTickUser(user, player)
        }
    }
}