package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.player.equipment.ArmorEquipEvent
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.world.block.event.BlockBreakActionEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

sealed interface ItemBehaviorHolder

interface ItemBehavior : ItemBehaviorHolder {

    val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>>

    /* Event handlers */

    // 除非特别说明，所有函数的 ItemStack 参数都保证已经是合法的 NekoItem

    fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) = Unit
    fun handleEntityInteract(player: Player, itemStack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) = Unit
    fun handleAttackEntity(player: Player, itemStack: ItemStack, attacked: Entity, event: EntityDamageByEntityEvent) = Unit
    fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) = Unit
    fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) = Unit
    fun handleBreak(player: Player, itemStack: ItemStack, event: PlayerItemBreakEvent) = Unit
    fun handleEquip(player: Player, itemStack: ItemStack, equipped: Boolean, event: ArmorEquipEvent) = Unit
    fun handleInventoryClick(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleInventoryClickOnCursor(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleInventoryHotbarSwap(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleBlockBreakAction(player: Player, itemStack: ItemStack, event: BlockBreakActionEvent) = Unit
    fun handleRelease(player: Player, itemStack: ItemStack, event: PlayerStopUsingItemEvent) = Unit
    fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) = Unit
    fun handleSkillPrepareCast(caster: Caster.Player, itemStack: ItemStack, skill: ConfiguredSkill, event: PlayerSkillPrepareCastEvent) = Unit
}

interface ItemBehaviorFactory<T : ItemBehavior> : ItemBehaviorHolder {
    /**
     * Creates a [ItemBehavior] with given [item] and [config].
     *
     * @param item the neko item
     * @param config the behavior config
     * @return an instance of [T]
     */
    fun create(item: NekoItem, config: ConfigProvider): T
}