package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.event.bukkit.NekoPreprocessDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.attackSpeed
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 原版剑横扫攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: sword
 * ```
 */
class SwordAttack(
    private val cancelVanillaDamage: Boolean,
) : AttackType {
    companion object {
        const val NAME = "sword"
    }

    override fun generateDamageMetadata(itemstack: NekoStack, event: NekoPreprocessDamageEvent): DamageMetadata? {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return null
        val playerAttributes = event.damagerAttributes
        val directDamageMetadata = PlayerDamageMetadata(
            attributes = playerAttributes,
            damageBundle = damageBundle(playerAttributes) {
                every {
                    standard()
                }
            }
        )

        return directDamageMetadata
    }

    override fun handleAttackEntity(itemstack: NekoStack, event: NekoPreprocessDamageEvent) {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return
        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleDamage(player: Player, itemstack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }
}