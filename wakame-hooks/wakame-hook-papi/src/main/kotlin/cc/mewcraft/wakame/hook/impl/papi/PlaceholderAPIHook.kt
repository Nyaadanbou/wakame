package cc.mewcraft.wakame.hook.impl.papi

import cc.mewcraft.wakame.KOISH_AUTHORS
import cc.mewcraft.wakame.KOISH_NAME
import cc.mewcraft.wakame.KOISH_VERSION
import cc.mewcraft.wakame.entity.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.user.toUser
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

@Hook(plugins = ["PlaceholderAPI"])
object PlaceholderAPIHook : PlaceholderExpansion() {

    init {
        register()
    }

    override fun getIdentifier(): String = KOISH_NAME
    override fun getAuthor(): String = KOISH_AUTHORS.joinToString()
    override fun getVersion(): String = KOISH_VERSION.toString()
    override fun getRequiredPlugin(): String = KOISH_NAME
    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        val user = player.toUser()
        when (params) {
            "mana" -> {
                val mana = user.resourceMap.current(ResourceTypeRegistry.MANA)
                return mana.toString()
            }
            "max_mana" -> {
                val maxMana = user.resourceMap.maximum(ResourceTypeRegistry.MANA)
                return maxMana.toString()
            }
        }
        return null
    }
}