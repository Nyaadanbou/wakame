package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key

object SkillRegistry : Initializable {
    /**
     * The key of the empty skill.
     */
    val EMPTY_KEY: Key = Key(NekoNamespaces.SKILL, "empty")

    override fun onPreWorld() {
        // placeholder code
    }
}
