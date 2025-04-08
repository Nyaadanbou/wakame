package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.entity.attribute.AttributeInstance
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.player.kizamiContainer
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.kizami2.KizamiMap
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


/**
 * 属性.
 *
 * 物品发生变化时, 根据物品核孔, 修改玩家的 [cc.mewcraft.wakame.entity.attribute.AttributeMap].
 */
internal object AttributeItemSlotChangeListener : ItemSlotChangeEventListener() {

    override val predicates: List<(Player, ItemSlot, ItemStack, NekoStack?) -> Boolean> = listOf(
        ::testSlot,
        ::testLevel,
        ::testDurability
    )

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyAttributeMap(player, slot, nekoStack) { instance, modifier -> instance.removeModifier(modifier) }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyAttributeMap(player, slot, nekoStack) { instance, modifier -> instance.addTransientModifier(modifier) }
    }

    private fun modifyAttributeMap(player: Player, slot: ItemSlot, nekoStack: NekoStack?, update: (AttributeInstance, AttributeModifier) -> Unit) {
        if (nekoStack == null)
            return
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, slot)
        val attributeMap = player.koishify()[AttributeMap]
        attributeModifiers.forEach { type, modifier ->
            val instance = attributeMap.getInstance(type)
            if (instance != null)
                update(instance, modifier)
        }
    }
}

/**
 * 铭刻.
 *
 * 物品发生变化时, 根据物品铭刻, 修改玩家的 [KizamiMap].
 */
internal object KizamiItemSlotChangeListener : ItemSlotChangeEventListener() {

    override val predicates: List<(Player, ItemSlot, ItemStack, NekoStack?) -> Boolean> = listOf(
        ::testSlot,
        ::testLevel,
        ::testDurability
    )

    // 首先, 从玩家身上移除所有已有的铭刻效果.
    // 我们将重新计算铭刻数量, 并将新的铭刻效果
    // (基于新的铭刻数量)应用到玩家身上.
    override fun onBegin(player: Player) {
        val kizamiContainer = player.kizamiContainer
        kizamiContainer.removeAllEffects(player)
    }

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyKizamiAmount(player, nekoStack) { kizamiMap, kizamizSet -> kizamiMap.subtractOneEach(kizamizSet) }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyKizamiAmount(player, nekoStack) { kizamiMap, kizamiSet -> kizamiMap.addOneEach(kizamiSet) }
    }

    // 基于当前铭刻数量, 将新的铭刻效果应用到玩家身上.
    override fun onEnd(player: Player) {
        val kizamiContainer = player.kizamiContainer
        kizamiContainer.applyAllEffects(player)
    }

    private fun modifyKizamiAmount(player: Player, nekoStack: NekoStack?, update: (KizamiMap, Set<RegistryEntry<Kizami>>) -> Unit) {
        val kizamiSet = nekoStack?.getKizamiz() ?: return
        val kizamiContainer = player.kizamiContainer
        update(kizamiContainer, kizamiSet)
    }

    private fun NekoStack.getKizamiz(): Set<RegistryEntry<Kizami>>? {
        return components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz
    }
}