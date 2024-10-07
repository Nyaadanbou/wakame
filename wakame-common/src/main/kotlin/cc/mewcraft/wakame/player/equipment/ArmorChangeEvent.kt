package cc.mewcraft.wakame.player.equipment

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class ArmorChangeEvent(
    player: Player,
    val slot: EquipmentSlot,
    val action: Action,
    val previous: ItemStack?,
    val current: ItemStack?,
) : PlayerEvent(player), Cancellable {
    private var cancel: Boolean = false

    override fun getHandlers(): HandlerList = HANDLER_LIST

    override fun isCancelled(): Boolean = cancel

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    enum class Action {
        /**
         * A player equipped a piece of armor.
         */
        EQUIP,

        /**
         * A player unequipped a piece of armor.
         */
        UNEQUIP,

        /**
         * A player replaced a piece of armor with another one.
         */
        CHANGE
    }

    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }
}