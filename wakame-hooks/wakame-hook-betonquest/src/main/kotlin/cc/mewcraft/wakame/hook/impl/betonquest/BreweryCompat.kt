// 用 Brewery 代替 TheBrewingProject 这个冗长的名字

package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.BrewQuestItemFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.BrewQuestItemSerializer
import cc.mewcraft.wakame.integration.Hook

@Hook(plugins = ["BetonQuest", "TheBrewingProject"], requireAll = true)
object BreweryCompat {

    init {
        hook {
            items {
                register("brew", BrewQuestItemFactory())
                registerSerializer("brew", BrewQuestItemSerializer())
            }
        }
    }
}