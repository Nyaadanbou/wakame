package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent

/**
 * 属性系统与事件系统的交互逻辑.
 */
class AttributeEventHandler : KoinComponent {

    /**
     * 当玩家装备的物品发生变化时, 执行的逻辑.
     */
    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        updateAttributeModifiers(player, oldItem, newItem) {
            it.slotGroup.contains(slot) && it.templates.has(ItemTemplateTypes.ATTRIBUTABLE)
        }
    }

    /**
     * 根据玩家之前“激活”的物品和当前“激活”的物品所提供的属性, 更新玩家的属性.
     *
     * 这里的新/旧指的是玩家先前“激活”的物品和当前“激活”的物品.
     *
     * @param player 玩家
     * @param oldItem 之前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param newItem 当前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param predicate 判断物品能否提供属性的谓词
     */
    private fun updateAttributeModifiers(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: (NekoStack) -> Boolean,
    ) {
        oldItem?.tryNekoStack?.removeAttributeModifiers(player, predicate)
        newItem?.tryNekoStack?.attachAttributeModifiers(player, predicate)
    }

    /**
     * 将该物品提供的 [AttributeModifier] 添加到 [player] 身上.
     *
     * @param player 要添加属性的玩家
     * @param predicate 判断物品能否提供属性的谓词
     * @receiver 可能提供属性的物品
     */
    private fun NekoStack.attachAttributeModifiers(player: Player, predicate: (NekoStack) -> Boolean) {
        if (!predicate(this)) {
            return
        }
        val userAttributes = player.toUser().attributeMap
        val itemCells = this.components.get(ItemComponentTypes.CELLS) ?: return
        val itemAttributes = itemCells.collectAttributeModifiers(this, ignoreCurse = true) // TODO 等诅咒完成后移除 ignoreCurse
        itemAttributes.forEach { attribute, modifier -> userAttributes.getInstance(attribute)?.addModifier(modifier) }
    }

    /**
     * 从 [player] 身上移除该物品提供的 [AttributeModifier].
     *
     * @param player 要移除属性的玩家
     * @param predicate 判断物品能否提供属性的谓词
     * @receiver 可能提供属性的物品
     */
    private fun NekoStack.removeAttributeModifiers(player: Player, predicate: (NekoStack) -> Boolean) {
        if (!predicate(this)) {
            return
        }
        val userAttributes = player.toUser().attributeMap
        val itemCells = this.components.get(ItemComponentTypes.CELLS) ?: return
        val itemAttributes = itemCells.collectAttributeModifiers(this, ignoreCurse = true)  // TODO 等诅咒完成后移除 ignoreCurse
        itemAttributes.forEach { attribute, modifier -> userAttributes.getInstance(attribute)?.removeModifier(modifier) }
    }
}
