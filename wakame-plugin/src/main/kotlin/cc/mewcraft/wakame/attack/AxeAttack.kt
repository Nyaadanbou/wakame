package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.entity.player.attackCooldownContainer
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.item2.ItemDamageEventMarker
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 原版斧单体攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: axe
 * ```
 */
class AxeAttack(
    private val cancelVanillaDamage: Boolean,
) : AttackType {
    companion object {
        const val NAME = "axe"
    }

    override fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        if (player.attackCooldownContainer.isActive(nekoStack.id)) {
            return null
        }

        val attributes = player.attributeContainer
        val damageMeta = PlayerDamageMetadata(
            attributes = attributes,
            damageBundle = damageBundle(attributes) {
                every {
                    standard()
                }
            }
        )

        return damageMeta
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        if (player.attackCooldownContainer.isActive(nekoStack.id)) {
            return
        }

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }
}