package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BKizamiMeta
import cc.mewcraft.wakame.item.binary.meta.getOrEmpty
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.asNekoUser
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

interface KizamiProvider : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<KizamiProvider> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): KizamiProvider {
            return Default()
        }
    }

    private class Default : KizamiProvider {
        override val requiredMetaTypes: Array<KClass<out SchemaItemMeta<*>>> = emptyArray()

        override fun handleItemHeld(player: Player, itemStack: ItemStack, event: PlayerItemHeldEvent) {
            val previousSlot = event.previousSlot
            val newSlot = event.newSlot
            val user = player.asNekoUser()
            resetKizamiEffects(user)
            itemStack.addKizamiAmount(user) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
            applyKizamiEffects(user)
        }

        override fun handleItemUnHeld(player: Player, itemStack: ItemStack, event: PlayerItemHeldEvent) {
            val previousSlot = event.previousSlot
            val newSlot = event.newSlot
            val user = player.asNekoUser()
            resetKizamiEffects(user)
            itemStack.subtractKizamiAmount(user) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
            applyKizamiEffects(user)
        }

        override fun handleSlotChangeNew(player: Player, itemStack: ItemStack, event: PlayerInventorySlotChangeEvent) {
            val slot = event.slot
            val rawSlot = event.rawSlot
            val user = player.asNekoUser()
            resetKizamiEffects(user)
            itemStack.addKizamiAmount(user) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
            applyKizamiEffects(user)
        }

        override fun handleSlotChangeOld(player: Player, itemStack: ItemStack, event: PlayerInventorySlotChangeEvent) {
            val slot = event.slot
            val rawSlot = event.rawSlot
            val user = player.asNekoUser()
            resetKizamiEffects(user)
            itemStack.subtractKizamiAmount(user) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
            applyKizamiEffects(user)
        }

        ////// Private Func //////

        private fun resetKizamiEffects(user: User<Player>) {
            val kizamiMap = user.kizamiMap

            // remove all the old kizami effects from the player
            kizamiMap.getImmutableAmountMap().forEach { (kizami, amount) ->
                KizamiRegistry.getEffect(kizami, amount).remove(kizami, user)
            }
        }

        /**
         * Updates the player states with the new and old ItemStacks.
         *
         * This function essentially applies kizami effects to the player,
         * such as updating attribute modifiers and active skills.
         *
         * @param user
         */
        private fun applyKizamiEffects(user: User<Player>) {
            val kizamiMap = user.kizamiMap

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
                println("${user.player.name}'s KizamiMap: " + newAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
            }
        }

        /**
         * Gets attribute modifiers on the ItemStack, considering the item's effective slot.
         */
        private inline fun ItemStack.getKizamiSet(testEffectiveness: NekoStack.() -> Boolean): Set<Kizami> {
            val nekoStack = NekoStackFactory.by(this) ?: return emptySet()

            if (!nekoStack.testEffectiveness()) {
                return emptySet()
            }

            val kizamiSet = nekoStack.getMetaAccessor<BKizamiMeta>().getOrEmpty()
            return kizamiSet
        }

        /**
         * Add the attribute modifiers of [this] to the [user].
         *
         * @param user the user we add attribute modifiers to
         */
        private inline fun ItemStack?.addKizamiAmount(user: User<Player>, testEffectiveness: NekoStack.() -> Boolean) {
            if (this == null || this.isEmpty)
                return
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
            if (this == null || this.isEmpty)
                return
            val kizamiSet = this.getKizamiSet(testEffectiveness)
            val kizamiMap = user.kizamiMap
            kizamiMap.subtractOneEach(kizamiSet)
        }
    }
}