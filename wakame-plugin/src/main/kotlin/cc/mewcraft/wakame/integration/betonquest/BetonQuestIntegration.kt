package cc.mewcraft.wakame.integration.betonquest

import org.bukkit.entity.Player

interface BetonQuestIntegration {

    companion object Impl : BetonQuestIntegration {
        private var implementation: BetonQuestIntegration = object : BetonQuestIntegration {
            override fun inConversation(player: Player): Boolean {
                return false
            }
        }

        fun setImplementation(impl: BetonQuestIntegration) {
            implementation = impl
        }

        override fun inConversation(player: Player): Boolean {
            return implementation.inConversation(player)
        }
    }

    fun inConversation(player: Player): Boolean
}