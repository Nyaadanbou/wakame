@file:JvmName("CooldownExt")

package cc.mewcraft.wakame.item2.extension

import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.entity.player.itemCooldownContainer
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.item2.typeId
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// ------------
// ItemStack
// ------------

fun ItemStack.isOnCooldown(player: Player): Boolean = toNMS().isOnCooldown(player)
fun ItemStack.getCooldownRemainingRatio(player: Player): Float = toNMS().getCooldownRemainingRatio(player)
fun ItemStack.addCooldown(player: Player, ticks: Int) = toNMS().addCooldown(player, ticks)
fun ItemStack.addCooldown(player: Player, speed: AttackSpeed) = toNMS().addCooldown(player, speed)
fun ItemStack.removeCooldown(player: Player) = toNMS().removeCooldown(player)

// ------------
// MojangStack
// ------------

// Implemented according to: https://github.com/Nyaadanbou/wakame/issues/369
private fun MojangStack.acquireCooldownGroup(): Identifier {
    return getProperty(ItemPropertyTypes.COOLDOWN_GROUP) ?: typeId
}

/**
 * @see cc.mewcraft.wakame.entity.player.ItemCooldownContainer.isActive
 */
fun MojangStack.isOnCooldown(player: Player): Boolean {
    return player.itemCooldownContainer.isActive(acquireCooldownGroup())
}

/**
 * @see cc.mewcraft.wakame.entity.player.ItemCooldownContainer.getRemainingRatio
 */
fun MojangStack.getCooldownRemainingRatio(player: Player): Float {
    return player.itemCooldownContainer.getRemainingRatio(acquireCooldownGroup())
}

/**
 * @see cc.mewcraft.wakame.entity.player.ItemCooldownContainer.activate
 */
fun MojangStack.addCooldown(player: Player, ticks: Int) {
    player.itemCooldownContainer.activate(acquireCooldownGroup(), ticks)
}

/**
 * @see cc.mewcraft.wakame.entity.player.ItemCooldownContainer.activate
 */
fun MojangStack.addCooldown(player: Player, speed: AttackSpeed) {
    player.itemCooldownContainer.activate(acquireCooldownGroup(), speed)
}

/**
 * @see cc.mewcraft.wakame.entity.player.ItemCooldownContainer.reset
 */
fun MojangStack.removeCooldown(player: Player) {
    player.itemCooldownContainer.reset(acquireCooldownGroup())
}
