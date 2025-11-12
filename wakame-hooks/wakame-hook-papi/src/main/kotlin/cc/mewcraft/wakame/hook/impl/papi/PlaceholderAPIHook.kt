package cc.mewcraft.wakame.hook.impl.papi

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

@Hook(plugins = ["PlaceholderAPI"])
object PlaceholderAPIHook : PlaceholderExpansion() {

    private val dimensionKeyMappings: Map<Key, String> by MAIN_CONFIG.entry<Map<Key, String>>("dimension_key_mappings")

    init {
        register()
    }

    override fun getIdentifier(): String = "Koish"
    override fun getAuthor(): String = BootstrapContexts.PLUGIN_AUTHORS.joinToString()
    override fun getVersion(): String = BootstrapContexts.PLUGIN_VERSION.toString()
    override fun getRequiredPlugin(): String? = null
    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        if (params == "mana") {
            val current = PlayerManaIntegration.getMana(player)
            return current.toString()
        } else if (params == "max_mana") {
            val maximum = PlayerManaIntegration.getMaxMana(player)
            return maximum.toString()
        } else if (params == "mana_percentage") {
            val current = PlayerManaIntegration.getMana(player)
            val maximum = PlayerManaIntegration.getMaxMana(player)
            return if (maximum > 0) (current / maximum * 100).toString() else "0"
        } else if (params == "current_dimension") {
            val worldKey = player.world.key
            return dimensionKeyMappings[worldKey] ?: worldKey.asString()
        } else if (params == "player_level") {
            return PlayerLevelIntegration.getOrDefault(player.uniqueId, 0).toString()
        } else if (params.startsWith("attribute_")) {
            val param0 = params.substringAfter("attribute_")
            val attributeMap = player.attributeContainer
            val attribute = Attributes.get(param0) ?: return null
            return attributeMap.getValue(attribute).toString()
        }

        return null
    }
}