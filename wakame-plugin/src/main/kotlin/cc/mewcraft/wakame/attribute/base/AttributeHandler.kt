package cc.mewcraft.wakame.attribute.base

import cc.mewcraft.wakame.event.WakameSlotChangedEvent
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import me.lucko.helper.terminable.Terminable
import me.lucko.helper.terminable.TerminableConsumer
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

// TODO this class is something to be heavily optimized in the future
//  ATM, we just make it work
/**
 * Handles the update process of [AttributeMap].
 *
 * Specifically, it handles custom attributes from neko items:
 * - which should be reflected in [AttributeMap] in real-time.
 * - which must be applied to players as real vanilla attribute modifiers.
 */
class AttributeHandler : KoinComponent, Terminable, TerminableConsumer,
    Listener {

    private val playerAttributeAccessor: PlayerAttributeAccessor by inject()
    private val compositeTerminable: CompositeTerminable = CompositeTerminable.create()

    ////// Listeners //////

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        playerAttributeAccessor.removeAttributeMap(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onHoldItem(e: PlayerItemHeldEvent) {
        val player = e.player
        val oldItem = player.inventory.getItem(e.previousSlot)
        val newItem = player.inventory.getItem(e.newSlot)
        removeAttributeModifiers(oldItem, player)
        addAttributeModifiers(newItem, player)
    }

    @EventHandler
    fun onInventoryChange(e: WakameSlotChangedEvent) {
        val player = e.player
        val slot = e.slot
        val oldItem = e.oldItem
        val newItem = e.newItem
        if (slot in getEffectiveNmsSlot(player)) {
            removeAttributeModifiers(oldItem, player)
            addAttributeModifiers(newItem, player)
        }
    }

    ////// Private Func //////

    private fun getEffectiveNmsSlot(player: Player): Set<Int> {
        return setOf(
            1, // Crafting grid 1 (top-left)
            5, // Player helmet
            6, // Player chestplate
            7, // Player leggings
            8, // Player boots
            player.inventory.heldItemSlot + 36 // Player held item
        )
    }

    private fun getNekoItemStack(bukkitItem: ItemStack?): NekoItemStack? {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            return null
        }

        val neko = NekoItemStackFactory.wrap(bukkitItem)
        if (neko.isNotNeko) {
            return null
        }

        return neko
    }

    private fun getAttributeModifiers(bukkitItem: ItemStack?): Multimap<out Attribute, AttributeModifier> {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            throw IllegalArgumentException("ItemStack must not be null, empty, or have no item meta.")
        }

        if (!bukkitItem.hasItemMeta()) {
            return ImmutableMultimap.of()
        }

        val neko = getNekoItemStack(bukkitItem)
            ?: return ImmutableMultimap.of()
        return neko.cells.getModifiers()
    }

    private fun addAttributeModifiers(bukkitItem: ItemStack?, player: Player) {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            return
        }
        val attributeModifiers = getAttributeModifiers(bukkitItem)
        val attributeMap = playerAttributeAccessor.getAttributeMap(player)
        attributeMap.addAttributeModifiers(attributeModifiers)
    }

    private fun removeAttributeModifiers(bukkitItem: ItemStack?, player: Player) {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            return
        }
        val attributeMap = playerAttributeAccessor.getAttributeMap(player)
        val neko = getNekoItemStack(bukkitItem) ?: return

        attributeMap.clearModifiers(neko.uuid)
    }

    override fun close() {
        get<Server>().onlinePlayers.forEach { playerAttributeAccessor.removeAttributeMap(it) }
        compositeTerminable.close()
    }

    override fun <T : AutoCloseable> bind(terminable: T): T {
        return compositeTerminable.bind(terminable)
    }
}
