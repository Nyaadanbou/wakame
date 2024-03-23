package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.meta.KizamiMeta
import cc.mewcraft.wakame.item.binary.meta.get
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.asNeko
import me.lucko.helper.cooldown.Cooldown
import me.lucko.helper.cooldown.CooldownMap
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class KizamiEventHandler {
    // This cooldown controls the highest frequency by which we calculate the kizami effects for a player
    private val kizamiUpdateCooldownMap: CooldownMap<UUID> = CooldownMap.create(Cooldown.ofTicks(20))

    /**
     * Updates attributes when the player switches their held item.
     */
    fun handlePlayerItemHeld(e: PlayerItemHeldEvent) {
        val player = e.player
        val oldItem = player.inventory.getItem(e.previousSlot)
        val newItem = player.inventory.getItem(e.newSlot)

        resetKizamiEffects(player, oldItem, newItem)
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

        if (shouldHandle(slot, rawSlot, player)) {
            resetKizamiEffects(player, oldItem, newItem)
        }
    }

    ////// Private Func //////

    /**
     * Updates the player states with the new and old ItemStacks.
     *
     * This function essentially applies kizami effects to the user,
     * such as updating attribute modifiers and active skills.
     */
    private fun resetKizamiEffects(player: Player, oldItem: ItemStack?, newItem: ItemStack?) {
        val user = player.asNeko()
        val kizamiMap = user.kizamiMap

        // remove all the old kizami effects
        val oldAmountMap = kizamiMap.getImmutableAmountMap().onEach { (kizami, amount) ->
            KizamiRegistry.getEffect(kizami, amount).remove(kizami, user)
        }

        // do calc to get the new final amount
        oldItem.subtractKizamiAmount(user)
        newItem.addKizamiAmount(user)

        // apply all the new kizami effects
        val newAmountMap = kizamiMap.getMutableAmountMap()
        val iterator = newAmountMap.iterator()
        while (iterator.hasNext()) {
            val (kizami, amount) = iterator.next()
            if (amount > 0) {
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
            println(newAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }
    }

    private fun shouldHandle(slot: Int, rawSlot: Int, player: Player): Boolean {
        return (slot == (player.inventory.heldItemSlot) && player.openInventory.getSlotType(rawSlot) == InventoryType.SlotType.QUICKBAR) ||
                (player.openInventory.getSlotType(rawSlot) == InventoryType.SlotType.ARMOR)
    }

    /**
     * Gets attribute modifiers on the ItemStack.
     */
    private fun ItemStack?.getKizamis(): Set<Kizami> {
        if (this == null || this.isEmpty) {
            throw IllegalArgumentException("ItemStack must not be null, empty, or it has no item meta")
        }

        if (!this.hasItemMeta()) {
            return emptySet()
        }

        val nekoStack = this.toNekoStack() ?: return emptySet()
        val kizamiSet = nekoStack.meta.get<KizamiMeta, _>().orEmpty()
        return kizamiSet
    }

    /**
     * Add the attribute modifiers of [this] for the [player].
     *
     * @param user the player we add attribute modifiers to
     */
    private fun ItemStack?.addKizamiAmount(user: User) {
        if (this == null || this.isEmpty) {
            return
        }

        val kizamiSet = this.getKizamis()
        val kizamiMap = user.kizamiMap
        kizamiMap.addOneEach(kizamiSet)
    }

    /**
     * Remove the attribute modifiers of [this] for the [player].
     *
     * @param user the player we remove attribute modifiers from
     */
    private fun ItemStack?.subtractKizamiAmount(user: User) {
        if (this == null || this.isEmpty) {
            return
        }

        val kizamiSet = this.getKizamis()
        val kizamiMap = user.kizamiMap
        kizamiMap.subtractOneEach(kizamiSet)
    }

    private fun ItemStack?.toNekoStack(): NekoStack? {
        if (this == null || this.isEmpty) {
            return null
        }

        val nekoStack = NekoStackFactory.wrap(this)
        if (nekoStack.isNotNeko) {
            return null
        }

        return nekoStack
    }
}