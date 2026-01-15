package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "LuckPerms"], requireAll = true)
object LuckPermsCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val loggerFactory = BetonQuest.getInstance().loggerFactory

        // Action
        val actionRegistry = plugin.questRegistries.action()

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
    }
}