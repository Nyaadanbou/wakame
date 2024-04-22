package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackPredicate
import cc.mewcraft.wakame.item.binary.playNekoStackOrNull
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.Castable
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.condition.PlayerSkillCastContext
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Handles skill triggers for players.
 */
class SkillEventHandler {

    fun onJump(player: Player, itemStack: ItemStack) {
        val skillMap = player.toUser().skillMap
        skillMap.getSkills(SkillTrigger.Jump).forEach {
            it.tryCast(PlayerSkillCastContext(CasterAdapter.adapt(player), TargetAdapter.adapt(player), itemStack))
        }
    }

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
            this.effectiveSlot.testItemHeld(player, previousSlot, newSlot) &&
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
            this.effectiveSlot.testInventorySlotChange(player, slot, rawSlot) &&
            this.hasBehavior<Castable>()
        }
    }

    /**
     * Updates skills.
     *
     * @param player
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     * @param predicate a function to test whether the item can provide attribute modifiers
     */
    private inline fun updateSkills(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: PlayNekoStackPredicate,
    ) {
        oldItem?.playNekoStackOrNull?.removeAttributeModifiers(player, predicate)
        newItem?.playNekoStackOrNull?.addAttributeModifiers(player, predicate)
    }

    /**
     * Add the skills of [this] for the [player].
     *
     * @param player the player we add skills to
     * @param predicate
     * @receiver the ItemStack which may provide skills
     */
    private inline fun PlayNekoStack.addAttributeModifiers(player: Player, predicate: PlayNekoStackPredicate) {
        if (!this.predicate()) {
            return
        }

        val skillMap = player.toUser().skillMap
        // TODO: 从物品获得需要添加的技能
        val skill = SkillRegistry.INSTANCE[Key(Namespaces.SKILL, "buff/potion_remove")]
        skillMap.setSkill(SkillTrigger.Jump, skill)
    }

    /**
     * Remove the skills of [this] for the [player].
     *
     * @param player the player we remove attribute modifiers from
     * @param predicate
     * @receiver the ItemStack which may provide skills
     */
    private inline fun PlayNekoStack.removeAttributeModifiers(player: Player, predicate: PlayNekoStackPredicate) {
        if (!this.predicate()) {
            return
        }

        val skillMap = player.toUser().skillMap
        // TODO: 从物品获得需要移除的技能 key
        skillMap.removeSkill(Key(Namespaces.SKILL, "buff/potion_remove"))
    }
}