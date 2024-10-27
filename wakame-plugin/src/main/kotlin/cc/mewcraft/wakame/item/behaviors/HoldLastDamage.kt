package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.skill.Skill
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

interface HoldLastDamage : ItemBehavior {
    private object Default : HoldLastDamage {
        override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
            tryCancelEvent(itemStack, player, wrappedEvent.event)
        }

        override fun handleEntityInteract(player: Player, itemStack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleEquip(player: Player, itemStack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleSkillPrepareCast(caster: Player, itemStack: ItemStack, skill: Skill, event: PlayerSkillPrepareCastEvent) {
            tryCancelEvent(itemStack, caster, event)
        }

        private fun tryCancelEvent(itemStack: ItemStack, player: Player, e: Cancellable) {
            val nekoStack = itemStack.tryNekoStack ?: return
            val maximumDamage = nekoStack.components.get(ItemComponentTypes.MAX_DAMAGE) ?: return
            val currentDamage = nekoStack.components.get(ItemComponentTypes.DAMAGE) ?: return
            if (currentDamage + 1 >= maximumDamage) {
                player.sendActionBar(text("无法使用完全损坏的物品"))
                e.isCancelled = true
            }
        }
    }

    companion object Type : ItemBehaviorType<HoldLastDamage> {
        override fun create(): HoldLastDamage {
            return Default
        }
    }
}