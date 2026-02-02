package cc.mewcraft.wakame.integration.externalmenu

import org.bukkit.entity.Player

interface ExternalMenu {

    fun open(player: Player, menuId: String, menuArgs: Array<String>, bypass: Boolean)

    companion object Impl : ExternalMenu {

        private var implementation: ExternalMenu = object : ExternalMenu {
            override fun open(player: Player, menuId: String, menuArgs: Array<String>, bypass: Boolean) = Unit
        }

        fun setImplementation(impl: ExternalMenu) {
            implementation = impl
        }

        override fun open(player: Player, menuId: String, menuArgs: Array<String>, bypass: Boolean) {
            implementation.open(player, menuId, menuArgs, bypass)
        }
    }
}