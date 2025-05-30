package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlotGroup
import cc.mewcraft.wakame.item2.config.property.impl.MinecraftItemSlot
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.extension.addCooldown
import cc.mewcraft.wakame.item2.extension.damageItem
import cc.mewcraft.wakame.item2.extension.isOnCooldown
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.getProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * 剑的物品行为.
 */
object Sword : Weapon {

    override fun handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) {
        if (itemstack.isOnCooldown(player)) return
        val sword = itemstack.getProperty(ItemPropertyTypes.SWORD) ?: return
        // 造成伤害
        val attrContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(attrContainer) {
            every {
                standard()
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 5.0, sword.attackHalfExtentsBase)
        if (hitEntities.isNotEmpty()) {
            // 造成伤害
            hitEntities.forEach { entity -> entity.hurt(damageMetadata, player, true) }
            // 设置耐久
            player.damageItem(EquipmentSlot.HAND, sword.itemDamagePerAttack)
        }
        // 设置冷却
        itemstack.addCooldown(player, sword.attackCooldown)
    }

    override fun handleRightClick(player: Player, itemstack: ItemStack, hand: EquipmentSlot, event: PlayerItemRightClickEvent) {
        // 右键事件不是主手触发的 - 不处理
        // 这样做的目的是确保副手剑的攻击是通过主手剑触发的
        // 防止主手不是剑也能右键使用副手的剑攻击
        if (hand != EquipmentSlot.HAND) {
            return
        }

        val itemInOffHand = player.inventory.itemInOffHand
        // 副手物品处于冷却 - 不处理
        if (itemInOffHand.isOnCooldown(player)) return
        // 副手物品不是剑 - 不处理
        val offSword = itemInOffHand.getProperty(ItemPropertyTypes.SWORD) ?: return

        val attributeContainerSnapshot = player.attributeContainer.getSnapshot()
        // 如果主手剑位于主手时提供属性修饰符, 才需要移除
        val coresOnMainSword = itemstack.getData(ItemDataTypes.CORE_CONTAINER)
        val slotGroup = itemstack.getProperty(ItemPropertyTypes.SLOT) ?: ItemSlotGroup.empty()
        if (slotGroup.contains(MinecraftItemSlot.MAINHAND)){
            // 移除主手剑上的属性修饰符
            val modifiersOnMainSword = coresOnMainSword?.collectAttributeModifiers(itemstack, MinecraftItemSlot.MAINHAND)
            if (modifiersOnMainSword != null) {
                attributeContainerSnapshot.removeModifiers(modifiersOnMainSword)
            }
        }

        // 副手剑上没有核心容器 - 不处理
        val coresOnOffSword = itemInOffHand.getData(ItemDataTypes.CORE_CONTAINER) ?: return
        val modifiersOnOffSword = coresOnOffSword.collectAttributeModifiers(itemInOffHand, ItemSlot.imaginary())
        // 加上副手剑上的属性修饰符
        attributeContainerSnapshot.addTransientModifiers(modifiersOnOffSword)

        // 造成伤害
        val damageMetadata = PlayerDamageMetadata(attributeContainerSnapshot) {
            every {
                standard()
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 5.0, offSword.attackHalfExtentsBase)
        if (hitEntities.isNotEmpty()) {
            // 造成伤害
            hitEntities.forEach { entity -> entity.hurt(damageMetadata, player, true) }
            // 设置耐久
            player.damageItem(EquipmentSlot.OFF_HAND, offSword.itemDamagePerAttack)
        }
        // 设置冷却
        itemInOffHand.addCooldown(player, offSword.attackCooldown)
        // 挥动副手动画
        player.swingOffHand()
    }
}