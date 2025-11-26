package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.InputCastableTrigger
import cc.mewcraft.wakame.item.property.impl.ItemSlotRegistry
import cc.mewcraft.wakame.item.property.impl.SpecialCastableTrigger
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInputEvent


@Init(stage = InitStage.POST_WORLD)
object CastableFeature : Listener {

    init {
        registerEvents()
    }

    // 开发日记 2025/11/24:
    // 用来实现 castable 中的以下触发器:
    // - special/on_equip
    // - special/on_unequip
    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val slot = event.slot
        val prev = event.oldItemStack
        val curr = event.newItemStack
        if (prev != null &&
            ItemSlotChanges.testSlot(slot, prev)
        ) {
            val castables = prev.getProp(ItemPropTypes.CASTABLE)
            if (castables != null) {
                for (castable in castables.values) {
                    val trigger = castable.trigger.unwrap()
                    if (trigger != SpecialCastableTrigger.ON_UNEQUIP)
                        continue
                    val skill = castable.skill
                    val manaCost = castable.manaCost
                    if (skill is SkillWrapper.Block) {
                        if (
                            SkillIntegration.isCooldown(player, skill.id, castable).not() &&
                            PlayerManaIntegration.consumeMana(player, manaCost)
                        ) {
                            skill.cast(player, castable)
                        }
                    } else {
                        if (
                            PlayerManaIntegration.consumeMana(player, manaCost)
                        ) {
                            skill.cast(player, castable)
                        }
                    }
                }
            }
        }
        if (curr != null &&
            ItemSlotChanges.testSlot(slot, curr) &&
            ItemSlotChanges.testLevel(player, curr) &&
            ItemSlotChanges.testDurability(curr)
        ) {
            val castables = curr.getProp(ItemPropTypes.CASTABLE)
            if (castables != null) {
                for (castable in castables.values) {
                    val trigger = castable.trigger.unwrap()
                    if (trigger != SpecialCastableTrigger.ON_EQUIP)
                        continue
                    val skill = castable.skill
                    val manaCost = castable.manaCost
                    if (skill is SkillWrapper.Block) {
                        if (
                            SkillIntegration.isCooldown(player, skill.id, castable).not() &&
                            PlayerManaIntegration.consumeMana(player, manaCost)
                        ) {
                            skill.cast(player, castable)
                        }
                    } else {
                        if (
                            PlayerManaIntegration.consumeMana(player, manaCost)
                        ) {
                            skill.cast(player, castable)
                        }
                    }
                }
            }
        }
    }

    // 开发日记 2025/11/24:
    // 用来实现 castable 中的以下触发器:
    // - input/forward
    // - input/backward
    // - input/left
    // - input/right
    // - input/jump
    // - input/sneak
    // - input/sprint
    @EventHandler
    fun on(event: PlayerInputEvent) {
        val player = event.player
        val input = event.input
        for (slot in ItemSlotRegistry.itemSlots()) {
            val item = slot.getItem(player) ?: continue
            val castables = item.getProp(ItemPropTypes.CASTABLE) ?: continue
            if (ItemSlotChanges.testSlot(slot, item) &&
                ItemSlotChanges.testLevel(player, item) &&
                ItemSlotChanges.testDurability(item)
            ) {
                for (castable in castables.values) {
                    val trigger = castable.trigger.unwrap()
                    if (trigger is InputCastableTrigger && trigger.test(input)) {
                        val skill = castable.skill
                        val manaCost = castable.manaCost
                        if (skill is SkillWrapper.Block) {
                            if (
                                SkillIntegration.isCooldown(player, skill.id, castable).not() &&
                                PlayerManaIntegration.consumeMana(player, manaCost)
                            ) {
                                skill.cast(player, castable)
                            }
                        } else {
                            if (PlayerManaIntegration.consumeMana(player, manaCost)) {
                                castable.skill.cast(player, castable)
                            }
                        }
                    }
                }
            }
        }
    }
}