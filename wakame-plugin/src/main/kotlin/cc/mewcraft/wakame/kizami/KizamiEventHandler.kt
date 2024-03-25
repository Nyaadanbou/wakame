package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.meta.KizamiMeta
import cc.mewcraft.wakame.item.binary.meta.get
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.asNekoUser
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack

class KizamiEventHandler {
    /**
     * Updates attributes when the player switches their held item.
     */
    fun handlePlayerItemHeld(e: PlayerItemHeldEvent) {
        val player = e.player
        val previousSlot = e.previousSlot
        val newSlot = e.newSlot
        val oldItem = player.inventory.getItem(previousSlot)
        val newItem = player.inventory.getItem(newSlot)

        resetKizamiEffects(player, oldItem, newItem) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
    }

    /**
     * Updates attributes when an ItemStack is changed in the player's inventory.
     */
    fun handlePlayerInventorySlotChange(e: PlayerInventorySlotChangeEvent) {
        val player = e.player
        val slot = e.slot
        val rawSlot = e.rawSlot
        val oldItem = e.oldItemStack
        val newItem = e.newItemStack

        resetKizamiEffects(player, oldItem, newItem) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
    }

    ////// Private Func //////

    /**
     * Updates the player states with the new and old ItemStacks.
     *
     * This function essentially applies kizami effects to the player,
     * such as updating attribute modifiers and active skills.
     *
     * @param player
     * @param oldItem
     * @param newItem
     * @param testSlot
     */
    private inline fun resetKizamiEffects(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        testSlot: NekoStack.() -> Boolean,
    ) {
        val user = player.asNekoUser()
        val kizamiMap = user.kizamiMap

        // remove all the old kizami effects from the player
        kizamiMap.getImmutableAmountMap().forEach { (kizami, amount) ->
            KizamiRegistry.getEffect(kizami, amount).remove(kizami, user)
        }

        // do calc to get the new kizami amount
        oldItem.subtractKizamiAmount(user, testSlot)
        newItem.addKizamiAmount(user, testSlot)

        val newAmountMap = kizamiMap.getMutableAmountMap()
        val iterator = newAmountMap.iterator()
        while (iterator.hasNext()) {
            val (kizami, amount) = iterator.next()
            if (amount > 0) {
                // apply new kizami effects to the player
                KizamiRegistry.getEffect(kizami, amount).apply(kizami, user)
            } else if (amount == 0) {
                // remove zero amount kizami so it won't be iterated again
                iterator.remove()
            } else {
                // put extra check here (in case you write bugs)
                error("Kizami amount should not be negative")
            }
        }

        if (newAmountMap.isNotEmpty()) { // remove it when stable
            println("${player.name}'s KizamiMap: " + newAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }
    }

    private fun ItemStack?.asNekoStack(): NekoStack? {
        if (this == null || this.isEmpty) {
            return null
        }

        val nekoStack = NekoStackFactory.wrap(this)
        if (nekoStack.isNotNeko) {
            return null
        }

        return nekoStack
    }

    /**
     * Gets attribute modifiers on the ItemStack, considering the item's effective slot.
     */
    private inline fun ItemStack.getKizamiSet(testEffectiveness: NekoStack.() -> Boolean): Set<Kizami> {
        if (!this.hasItemMeta()) {
            return emptySet()
        }

        val nekoStack = this.asNekoStack() ?: return emptySet()

        if (!nekoStack.testEffectiveness()) {
            return emptySet()
        }

        val kizamiSet = nekoStack.meta.get<KizamiMeta, _>().orEmpty()
        return kizamiSet
    }

    /**
     * Add the attribute modifiers of [this] to the [user].
     *
     * @param user the user we add attribute modifiers to
     */
    private inline fun ItemStack?.addKizamiAmount(user: User<Player>, testEffectiveness: NekoStack.() -> Boolean) {
        if (this == null || this.isEmpty) {
            return
        }

        val kizamiSet = this.getKizamiSet(testEffectiveness)
        val kizamiMap = user.kizamiMap
        kizamiMap.addOneEach(kizamiSet)
    }

    /**
     * Remove the attribute modifiers of the [this] to the [user].
     *
     * @param user the user we remove attribute modifiers from
     */
    private inline fun ItemStack?.subtractKizamiAmount(user: User<Player>, testEffectiveness: NekoStack.() -> Boolean) {
        if (this == null || this.isEmpty) {
            return
        }

        val kizamiSet = this.getKizamiSet(testEffectiveness)
        val kizamiMap = user.kizamiMap
        kizamiMap.subtractOneEach(kizamiSet)
    }
}