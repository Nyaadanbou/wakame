package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackPredicate
import cc.mewcraft.wakame.item.binary.playNekoStackOrNull
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.Castable
import cc.mewcraft.wakame.skill.condition.PlayerSkillCastContext
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Handles skill triggers for players.
 */
class SkillEventHandler {

    /* Handles skill triggers for players. */

    fun onLeftClick(player: Player, itemStack: ItemStack, location: Location?) {
        val skillMap = player.toUser().skillMap
        val target = location?.let { TargetAdapter.adapt(it) } ?: TargetAdapter.adapt(player)
        skillMap.getSkill(Trigger.LeftClick).forEach {
            it.tryCast(PlayerSkillCastContext(CasterAdapter.adapt(player), target, itemStack))
        }
    }

    fun onRightClick(player: Player, itemStack: ItemStack, location: Location?) {
        val skillMap = player.toUser().skillMap
        val target = location?.let { TargetAdapter.adapt(it) } ?: TargetAdapter.adapt(player)

        skillMap.getSkill(Trigger.RightClick).forEach {
            it.tryCast(PlayerSkillCastContext(CasterAdapter.adapt(player), target, itemStack))
        }
    }

    fun onJump(player: Player, itemStack: ItemStack) {
        val skillMap = player.toUser().skillMap
        skillMap.getSkill(Trigger.Jump).forEach {
            it.tryCast(PlayerSkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(player), itemStack))
        }
    }

    fun onAttack(player: Player, entity: LivingEntity, itemStack: ItemStack) {
        val skillMap = player.toUser().skillMap
        skillMap.getSkill(Trigger.Attack).forEach {
            it.tryCast(PlayerSkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(entity), itemStack))
        }
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
            this.slot.testInventorySlotChangeEvent(player, slot, rawSlot) &&
            this.hasBehavior<Castable>()
        }
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
        oldItem?.playNekoStackOrNull?.removeSkills(player, predicate)
        newItem?.playNekoStackOrNull?.addSkills(player, predicate)
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
        val skills = this.cell.getSkills(true) // TODO: remove if skill module is complete
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
        val skills = this.cell.getSkills(true)  // TODO: remove if skill module is complete
        skillMap.removeSkill(skills)
    }
}