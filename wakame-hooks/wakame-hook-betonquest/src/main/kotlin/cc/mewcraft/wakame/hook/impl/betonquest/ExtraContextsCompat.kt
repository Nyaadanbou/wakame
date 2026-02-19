package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.kv.DeleteKeyActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.kv.SetKeyValueActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.kv.HasKeyValueFactory
import cc.mewcraft.wakame.integration.Hook

@Hook(plugins = ["BetonQuest", "ExtraContexts"], requireAll = true)
object ExtraContextsCompat {

    init {
        hook {
            actions {
                register("delkv", DeleteKeyActionFactory(api.loggerFactory()))
                register("setkv", SetKeyValueActionFactory(api.loggerFactory()))
            }
            conditions {
                register("haskv", HasKeyValueFactory(api.loggerFactory()))
            }
        }
    }
}