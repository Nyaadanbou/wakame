package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackPredicate
import cc.mewcraft.wakame.item.binary.toNekoStack
import cc.mewcraft.wakame.item.binary.tryNekoStack
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.Castable
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.state.SkillStateResult
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Handles skill triggers for players.
 */
class SkillEventHandler {

    /* Handles skill triggers for players. */

    fun onLeftClickBlock(player: Player, itemStack: ItemStack, location: Location, event: PlayerInteractEvent) {
        onLeftClick(player, itemStack, event) { TargetAdapter.adapt(location) }
    }

    fun onLeftClickAir(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
        onLeftClick(player, itemStack, event) { TargetAdapter.adapt(player) }
    }

    private fun onLeftClick(player: Player, itemStack: ItemStack, event: PlayerInteractEvent, targetProvider: () -> Target) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        val target = targetProvider()
        val result = user.skillState.addTrigger(
            SingleTrigger.LEFT_CLICK,
            SkillCastContext(CasterAdapter.adapt(player), target, nekoStack)
        )
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    fun onRightClickBlock(player: Player, itemStack: ItemStack, location: Location, event: PlayerInteractEvent) {
        onRightClick(player, itemStack, event) { TargetAdapter.adapt(location) }
    }

    fun onRightClickAir(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
        onRightClick(player, itemStack, event) { TargetAdapter.adapt(player) }
    }

    private fun onRightClick(player: Player, itemStack: ItemStack, event: PlayerInteractEvent, targetProvider: () -> Target) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        val target = targetProvider.invoke()
        val result = user.skillState.addTrigger(
            SingleTrigger.RIGHT_CLICK,
            SkillCastContext(CasterAdapter.adapt(player), target, nekoStack)
        )
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    fun onJump(player: Player, itemStack: ItemStack) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        user.skillState.addTrigger(SingleTrigger.JUMP, SkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(player), nekoStack))
    }

    fun onAttack(player: Player, entity: LivingEntity, itemStack: ItemStack) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        user.skillState.addTrigger(SingleTrigger.ATTACK, SkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(entity), nekoStack))
    }

    /* Handles skill triggers for players. */

    /**
     * Updates skills when the player switches their held item.
     *
     * @param player
     * @param previousSlot
     * @param newSlot
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     */
    fun handlePlayerItemHeld(
        player: Player,
        previousSlot: Int,
        newSlot: Int,
        oldItem: ItemStack?,
        newItem: ItemStack?,
    ) {
        updateSkills(player, oldItem, newItem) {
            this.slot.testItemHeldEvent(player, previousSlot, newSlot) &&
                    this.hasBehavior<Castable>()
        }
        player.toUser().skillState.clear()
    }

    /**
     * Updates skills when an ItemStack is changed in the player's inventory.
     *
     * @param player
     * @param rawSlot
     * @param slot
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     */
    fun handlePlayerInventorySlotChange(
        player: Player,
        rawSlot: Int,
        slot: Int,
        oldItem: ItemStack?,
        newItem: ItemStack?,
    ) {
        updateSkills(player, oldItem, newItem) {
            this.slot.testInventorySlotChangeEvent(player, slot, rawSlot) && this.hasBehavior<Castable>()
        }
        player.toUser().skillState.clear()
    }

    /**
     * Updates skills.
     *
     * @param player
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     * @param predicate a function to test whether the item can provide skills
     */
    private inline fun updateSkills(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: PlayNekoStackPredicate,
    ) {
        oldItem?.tryNekoStack?.removeSkills(player, predicate)
        newItem?.tryNekoStack?.addSkills(player, predicate)
    }

    /**
     * Add the skills of [this] for the [player].
     *
     * @param player the player we add skills to
     * @param predicate
     * @receiver the ItemStack which may provide skills
     */
    private inline fun PlayNekoStack.addSkills(player: Player, predicate: PlayNekoStackPredicate) {
        if (!this.predicate()) {
            return
        }
        val skillMap = player.toUser().skillMap
        val skills = this.cell.getSkills(neglectCurse = true, neglectVariant = true) // TODO: remove if skill module is complete
        skillMap.addSkillsByInstance(skills)
    }

    /**
     * Remove the skills of [this] for the [player].
     *
     * @param player the player we remove skills from
     * @param predicate
     * @receiver the ItemStack which may provide skills
     */
    private inline fun PlayNekoStack.removeSkills(player: Player, predicate: PlayNekoStackPredicate) {
        if (!this.predicate()) {
            return
        }

        val skillMap = player.toUser().skillMap
        val skills = this.cell.getSkills(neglectCurse = true, neglectVariant = true)  // TODO: remove if skill module is complete
        skillMap.removeSkill(skills)
    }
}