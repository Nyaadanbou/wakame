package cc.mewcraft.wakame.integration.auraskills

import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import org.bukkit.entity.Player

/**
 * 使 Koish 的魔法值属性:
 * - [cc.mewcraft.wakame.entity.attribute.Attributes.MAX_MANA]
 * - [cc.mewcraft.wakame.entity.attribute.Attributes.MANA_REGENERATION]
 * 与 AuraSkills 的魔法值系统相关联.
 */
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