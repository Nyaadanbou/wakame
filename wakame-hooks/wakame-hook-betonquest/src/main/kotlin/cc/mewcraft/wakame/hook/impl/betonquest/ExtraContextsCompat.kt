package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.kv.DeleteKeyActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.kv.SetKeyValueActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.kv.HasKeyValueFactory
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "ExtraContexts"], requireAll = true)
object ExtraContextsCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val loggerFactory = BetonQuest.getInstance().loggerFactory

        /* Quest Type Registries */

        // Action
        val actionRegistry = plugin.questRegistries.action()
        actionRegistry.register("delkv", DeleteKeyActionFactory(loggerFactory))
        actionRegistry.register("setkv", SetKeyValueActionFactory(loggerFactory))

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("haskv", HasKeyValueFactory(loggerFactory))
    }
}