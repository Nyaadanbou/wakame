package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.item.binary.WakaItemStackFactory
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.Terminable
import me.lucko.helper.terminable.TerminableConsumer
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO this class is something to be heavily optimized in the future
//  ATM, we just make it work
/**
 * Handles the update process of [AttributeMap].
 *
 * Specifically, it handles custom attributes from wakame items:
 * - which should be reflected in [AttributeMap] in real-time.
 * - which must be applied to players as real vanilla attribute modifiers.
 */
class AttributeHandler : KoinComponent, Terminable, TerminableConsumer,
    Listener {
    private val playerAttributeAccessor: PlayerAttributeAccessor by inject()
    private val compositeTerminable: CompositeTerminable = CompositeTerminable.create()

    /**
     * Starts a background task which periodically reads [AttributeModifier]s
     * from the items in online players' inventories and adds these modifiers
     * to their [AttributeMap]s.
     */
    fun startUpdateTask() { // TODO 可能不需要???
        Schedulers.builder().sync().every(20).run {
            for (player in Bukkit.getOnlinePlayers()) {
                applyArmorAttributeModifiers(player)
                applyMainHandAttributeModifiers(player)
            }
        }.bindWith(this)
    }

    ////// Listeners //////

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        applyArmorAttributeModifiers(player)
        applyMainHandAttributeModifiers(player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        playerAttributeAccessor.removeAttributeMap(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEquipArmor(e: PlayerArmorChangeEvent) {
        val player = e.player
        val oldItem = e.oldItem
        val newItem = e.newItem
        if (oldItem.isEmpty && !newItem.isEmpty) {
            // 原本没穿，换了新的
            addAttributeModifiers(newItem, player)
        } else if (!oldItem.isEmpty && newItem.isEmpty) {
            // 原本穿了，脱下来了
            removeAttributeModifiers(oldItem, player)
        } else if (!newItem.isEmpty && !oldItem.isEmpty) {
            // 原本穿了，换了新的
            removeAttributeModifiers(oldItem, player)
            addAttributeModifiers(newItem, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHoldItem(e: PlayerItemHeldEvent) {
        val player = e.player
        val inventory = player.inventory
        val prevItem = inventory.getItem(e.previousSlot)
        val currItem = inventory.getItem(e.newSlot)
        removeAttributeModifiers(prevItem, player)
        addAttributeModifiers(currItem, player)
    }

    ////// Private Func //////

    /**
     * Applies attributes from armor contents.
     */
    private fun applyArmorAttributeModifiers(player: Player) {
        for (armor in player.inventory.armorContents) {
            armor ?: continue // skip AIR
            val attributeModifiers = getAttributeModifiers(armor)
            val attributeMap = playerAttributeAccessor.getAttributeMap(player)
            attributeMap.addAttributeModifiers(attributeModifiers)
        }
    }

    /**
     * Applies attributes from the item in main hand.
     */
    private fun applyMainHandAttributeModifiers(player: Player) {
        val bukkitItem = player.inventory.itemInMainHand
        addAttributeModifiers(bukkitItem, player)
    }

    private fun getAttributeModifiers(bukkitItem: ItemStack?): Multimap<out Attribute, AttributeModifier> {
        if (bukkitItem == null || bukkitItem.isEmpty || !bukkitItem.hasItemMeta()) {
            return ImmutableMultimap.of()
        }

        val waka = WakaItemStackFactory.wrap(bukkitItem)
        if (waka.isNotWakame) {
            return ImmutableMultimap.of()
        }

        return waka.slotAccessor.getModifiers()
    }

    private fun addAttributeModifiers(bukkitItem: ItemStack?, player: Player) {
        val attributeModifiers = getAttributeModifiers(bukkitItem)
        val attributeMap = playerAttributeAccessor.getAttributeMap(player)
        attributeMap.addAttributeModifiers(attributeModifiers)
    }

    private fun removeAttributeModifiers(bukkitItem: ItemStack?, player: Player) {
        val attributeModifiers = getAttributeModifiers(bukkitItem)
        val attributeMap = playerAttributeAccessor.getAttributeMap(player)
        attributeMap.removeAttributeModifiers(attributeModifiers)
    }

    override fun close() {
        compositeTerminable.close()
    }

    override fun <T : AutoCloseable> bind(terminable: T): T {
        return compositeTerminable.bind(terminable)
    }
}
