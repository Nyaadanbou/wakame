package cc.mewcraft.wakame.attribute.handler

import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getStringOrNull
import cc.mewcraft.wakame.util.readNbtOrNull
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import me.lucko.helper.Schedulers
import me.lucko.helper.plugin.HelperPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

/**
 * Handles the update process of [PlayerAttributeMap].
 *
 * Specifically, it handles:
 * - custom attributes from wakame items which should be reflected in [PlayerAttributeMap].
 * - custom attributes from wakame items that must be applied to players as real vanilla attribute modifiers.
 */
class PlayerAttributeHandler(
    private val plugin: HelperPlugin,
    private val accessor: PlayerAttributeAccessor,
) : Listener {
    /**
     * Starts a task that periodically computes the [PlayerAttributeMap] for all online players.
     */
    fun startDemon() {
        Schedulers.builder().sync().every(20).run {
            for (player in Bukkit.getOnlinePlayers()) {
                applyArmor(player.inventory.armorContents)
                applyHand(player.inventory.itemInMainHand)
            }
        }.bindWith(plugin)
    }

    // --- Listeners ---

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        e.player.inventory.run {
            applyArmor(armorContents)
            applyHand(itemInMainHand)
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        e.player.inventory.run {

        }
    }

    @EventHandler
    fun onEquip(e: PlayerArmorChangeEvent) {
        e.player.inventory.run {

        }
    }

    /**
     * Applies attributes from armor contents.
     */
    private fun applyArmor(armorContents: Array<ItemStack?>) {
        for (armor in armorContents) {
            val tag = armor?.readNbtOrNull() ?: continue
            val wakameTag = tag.getCompoundOrNull("wakame") ?: continue
            with(wakameTag) read@{
                val namespace = getStringOrNull("namespace") ?: return@read
                val id = getStringOrNull("id") ?: return@read
                if (namespace != "armor") return@read
            }
        }
    }

    /**
     * Applies attributes from item in hand.
     */
    private fun applyHand(itemInHand: ItemStack) {

    }
}