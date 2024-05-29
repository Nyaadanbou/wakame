package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackPredicate
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BKizamiMeta
import cc.mewcraft.wakame.item.binary.meta.getOrEmpty
import cc.mewcraft.wakame.item.binary.playNekoStackOrNull
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.KizamiProvider
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry.getBy
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class KizamiEventHandler {
    /**
     * Updates kizami effects when the player switches their held item.
     *
     * @param player
     * @param previousSlot
     * @param newSlot
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     */
    fun handlePlayerItemHeld(player: Player, previousSlot: Int, newSlot: Int, oldItem: ItemStack?, newItem: ItemStack?) {
        updateKizamiEffects(player, oldItem, newItem) {
            this.slot.testItemHeld(player, previousSlot, newSlot) &&
            this.hasBehavior<KizamiProvider>()
        }
    }

    /**
     * Updates kizami effects when an ItemStack is changed in the player's inventory.
     *
     * @param player
     * @param rawSlot
     * @param slot
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     */
    fun handlePlayerInventorySlotChange(player: Player, rawSlot: Int, slot: Int, oldItem: ItemStack?, newItem: ItemStack?) {
        updateKizamiEffects(player, oldItem, newItem) {
            this.slot.testInventorySlotChange(player, slot, rawSlot) &&
            this.hasBehavior<KizamiProvider>()
        }
    }

    /**
     * Updates the player states with the new and old ItemStacks.
     *
     * This function essentially applies kizami effects to the player,
     * such as updating attribute modifiers and active skills.
     *
     * @param player
     * @param oldItem
     * @param newItem
     * @param predicate a function to test whether the item can provide kizami
     */
    private inline fun updateKizamiEffects(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: PlayNekoStackPredicate,
    ) {
        val oldNekoStack = oldItem?.playNekoStackOrNull
        val newNekoStack = newItem?.playNekoStackOrNull

        // Optimization:
        // if old item and new item are both null,
        // we can fast return.
        if (oldNekoStack == null && newNekoStack == null) {
            return
        }

        val user = player.toUser()
        val kizamiMap = user.kizamiMap

        // First, remove all the existing kizami effects from the player,
        // since we will recalculate the kizami map and apply new kizami
        // effects (based on the new kizami map) to the player.
        val immutableAmountMap = kizamiMap.immutableAmountMap
        immutableAmountMap.forEach { (kizami, amount) ->
            KizamiRegistry.EFFECTS.getBy(kizami, amount).remove(kizami, user)
        }

        if (immutableAmountMap.isNotEmpty()) { // debug message - remove it when all done
            player.sendMessage("${Bukkit.getCurrentTick()} - your kizami (old): " + immutableAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }

        // Recalculate the kizami map.
        // The algorithm is simple:
        // subtract kizami amount, based on the old item,
        // then add kizami amount, based on the new item.
        oldNekoStack?.subtractKizamiAmount(user, predicate)
        newNekoStack?.addKizamiAmount(user, predicate)

        val mutableAmountMap = kizamiMap.mutableAmountMap
        val iterator = mutableAmountMap.iterator()
        while (iterator.hasNext()) {
            val (kizami, amount) = iterator.next()
            if (amount > 0) {
                // apply new kizami effects to the player
                KizamiRegistry.EFFECTS.getBy(kizami, amount).apply(kizami, user)
            } else {
                // optimization: remove kizami with zero and negative amount, so it won't be iterated again
                iterator.remove()
            }
        }

        if (mutableAmountMap.isNotEmpty()) { // debug message - remove it when all done
            player.sendMessage("${Bukkit.getCurrentTick()} - your kizami (new): " + mutableAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }
    }

    /**
     * Add the kizami of [this] to the [user].
     *
     * @receiver the ItemStack which may provide kizami
     * @param user the user we remove kizami from
     * @param predicate
     */
    private inline fun PlayNekoStack.addKizamiAmount(user: User<Player>, predicate: PlayNekoStackPredicate) {
        val kizamiSet = this.getKizamiSet(predicate)
        val kizamiMap = user.kizamiMap
        kizamiMap.addOneEach(kizamiSet)
    }

    /**
     * Remove the kizami of the [this] to the [user].
     *
     * @receiver the ItemStack which may provide kizami
     * @param user the user we remove kizami from
     * @param predicate
     */
    private inline fun PlayNekoStack.subtractKizamiAmount(user: User<Player>, predicate: PlayNekoStackPredicate) {
        val kizamiSet = this.getKizamiSet(predicate)
        val kizamiMap = user.kizamiMap
        kizamiMap.subtractOneEach(kizamiSet)
    }

    /**
     * Gets all kizami on [this], based on the result of [predicate].
     *
     * @receiver the ItemStack which may provide kizami
     * @param predicate
     * @return all kizami on the ItemStack
     */
    private inline fun PlayNekoStack.getKizamiSet(predicate: PlayNekoStackPredicate): Set<Kizami> {
        if (!this.predicate()) {
            return emptySet()
        }

        val kizamiSet = this.getMetaAccessor<BKizamiMeta>().getOrEmpty()
        return kizamiSet
    }
}