package cc.mewcraft.wakame.hook.impl.papi

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.integration.Hook
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

@Hook(plugins = ["PlaceholderAPI"])
object PlaceholderAPIHook : PlaceholderExpansion() {

    init {
        register()
    }

    override fun getIdentifier(): String = BootstrapContexts.PLUGIN_NAME.lowercase()
    override fun getAuthor(): String = BootstrapContexts.PLUGIN_AUTHORS.joinToString()
    override fun getVersion(): String = BootstrapContexts.PLUGIN_VERSION.toString()
    override fun getRequiredPlugin(): String = BootstrapContexts.PLUGIN_NAME
    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        val paramsList = params.lowercase().split("_", limit = 2)
        when (paramsList[0]) {
            "mana" -> {
                val current = player.koishify()[Mana].current
                return current.toString()
            }

            "max_mana" -> {
                val maximum = player.koishify()[Mana].maximum
                return maximum.toString()
            }

            "mana_percentage" -> {
                val current = player.koishify()[Mana].current
                val maximum = player.koishify()[Mana].maximum
                return if (maximum > 0) {
                    (current.toDouble() / maximum * 100).toString()
                } else {
                    .0.toString()
                }
            }
            "attribute" -> {
                if (paramsList.size < 2)
                    return null
                val attributeMap = player.koishify()[AttributeMap]
                val attribute = Attributes.get(paramsList[1]) ?: return null
                return attributeMap.getValue(attribute).toString()
            }
        }
        return null
    }
}