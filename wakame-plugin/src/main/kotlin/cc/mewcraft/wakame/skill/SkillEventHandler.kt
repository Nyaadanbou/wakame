package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackPredicate
import cc.mewcraft.wakame.item.binary.playNekoStackOrNull
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.Castable
import cc.mewcraft.wakame.skill.context.SkillCastContextBuilder
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.hasCombo
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Handles skill triggers for players.
 */
class SkillEventHandler(
    private val skillCastManager: SkillCastManager
) {

    /* Handles skill triggers for players. */

    fun onLeftClickBlock(player: Player, itemStack: ItemStack, location: Location) {
        onLeftClick(player, itemStack) { TargetAdapter.adapt(location) }
    }

    fun onLeftClickAir(player: Player, itemStack: ItemStack) {
        onLeftClick(player, itemStack) { TargetAdapter.adapt(player) }
    }

    private fun onLeftClick(player: Player, itemStack: ItemStack, targetProvider: () -> Target) {
        val user = player.toUser()
        val skillMap = user.skillMap
        val target = targetProvider.invoke()
        skillMap.getSkill(Trigger.LeftClick).forEach { skill: Skill ->
            skillCastManager.tryCast(skill, SkillCastContextBuilder.createPlayerSkillCastContext(CasterAdapter.adapt(player), target, itemStack))
        }
        if (!skillMap.getTriggers().hasCombo()) return
        user.skillStateManager.addTrigger(Trigger.LeftClick) { skill ->
            skillCastManager.tryCast(skill, SkillCastContextBuilder.createPlayerSkillCastContext(CasterAdapter.adapt(player), target, itemStack))
                .isSuccessful()
        }
    }

    fun onRightClickBlock(player: Player, itemStack: ItemStack, location: Location) {
        onRightClick(player, itemStack) { TargetAdapter.adapt(location) }
    }

    fun onRightClickAir(player: Player, itemStack: ItemStack) {
        onRightClick(player, itemStack) { TargetAdapter.adapt(player) }
    }

    private fun onRightClick(player: Player, itemStack: ItemStack, targetProvider: () -> Target) {
        val user = player.toUser()
        val skillMap = user.skillMap
        val target = targetProvider.invoke()
        skillMap.getSkill(Trigger.RightClick).forEach { skill ->
            skillCastManager.tryCast(skill, SkillCastContextBuilder.createPlayerSkillCastContext(CasterAdapter.adapt(player), target, itemStack))
        }
        if (!skillMap.getTriggers().hasCombo()) return
        user.skillStateManager.addTrigger(Trigger.RightClick) { skill ->
            skillCastManager.tryCast(skill, SkillCastContextBuilder.createPlayerSkillCastContext(CasterAdapter.adapt(player), target, itemStack))
                .isSuccessful()
        }
    }

    fun onJump(player: Player, itemStack: ItemStack) {
        val skillMap = player.toUser().skillMap
        skillMap.getSkill(Trigger.Jump).forEach { skill ->
            skillCastManager.tryCast(skill, SkillCastContextBuilder
                .createPlayerSkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(player), itemStack)
            )
        }
    }

    fun onAttack(player: Player, entity: LivingEntity, itemStack: ItemStack) {
        val skillMap = player.toUser().skillMap
        skillMap.getSkill(Trigger.Attack).forEach { skill ->
            skillCastManager.tryCast(skill, SkillCastContextBuilder
                .createPlayerSkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(entity), itemStack)
            )
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
        player.toUser().skillStateManager.clear()
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
        player.toUser().skillStateManager.clear()
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