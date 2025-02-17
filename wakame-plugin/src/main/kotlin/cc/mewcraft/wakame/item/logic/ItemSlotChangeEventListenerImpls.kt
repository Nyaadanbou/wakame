package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.attribute.AttributeInstance
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.enchantment2.getEffects
import cc.mewcraft.wakame.enchantment2.koishEnchantments
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.playerAbilities
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.mixin.support.EnchantmentAttributeEffect
import cc.mewcraft.wakame.mixin.support.EnchantmentEffectComponentsPatch
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.collections.takeUnlessEmpty

/**
 * 攻击速度.
 *
 * 物品发生变化时, 根据物品攻击速度, 更新所有必要的状态.
 */
internal object AttackSpeedItemSlotChangeListener : ItemSlotChangeEventListener() {

    override val predicates: List<(Player, ItemSlot, ItemStack, NekoStack?) -> Boolean> = listOf(
        ::testSlot,
        ::testLevel,
        ::testDurability
    )

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        nekoStack?.handleAttackSpeed { }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        nekoStack?.handleAttackSpeed { }
    }

    private fun NekoStack.handleAttackSpeed(block: (AttackSpeedLevel) -> Unit) {
        components.get(ItemComponentTypes.ATTACK_SPEED)?.level?.apply(block)
    }
}

/**
 * 属性.
 *
 * 物品发生变化时, 根据物品核孔, 修改玩家的 [cc.mewcraft.wakame.attribute.AttributeMap].
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
        val attributeMap = player.toUser().attributeMap
        attributeModifiers.forEach { type, modifier ->
            val instance = attributeMap.getInstance(type)
            if (instance != null)
                update(instance, modifier)
        }
    }
}

/**
 * 附魔.
 *
 * 物品发生变化时, 根据物品附魔, 应用与移除相应的附魔效果.
 */
internal object EnchantmentItemSlotChangeListener : ItemSlotChangeEventListener() {
    // 在 1.20.5 以后, 可以通过数据包添加自定义的魔咒.
    // 这也意味着我们不再需要手动处理与附魔相关的机制, 例如铁砧.
    // 唯一需要处理的就是监听物品栏发生的变化, 以应用附魔的效果.

    override val predicates: List<(Player, ItemSlot, ItemStack, NekoStack?) -> Boolean> = listOf(
        ::testSlot, // 要让魔咒生效, 必须让 Koish 物品位于生效的装备槽位上, 与魔咒本身的数据包定义无关
        ::testLevel,
        ::testDurability
    )

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyEnchantmentEffects(player, slot, itemStack) { xlevel, xplayer, xslot, effect ->
            effect.remove(xlevel, xslot, xplayer)
        }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyEnchantmentEffects(player, slot, itemStack) { xlevel, xplayer, xslot, effect ->
            effect.apply(xlevel, xslot, xplayer)
        }
    }

    private fun modifyEnchantmentEffects(
        player: Player, slot: ItemSlot, itemStack: ItemStack,
        action: (level: Int, player: Player, slot: ItemSlot, effect: EnchantmentAttributeEffect) -> Unit,
    ) {
        val enchantments = itemStack.koishEnchantments
        for ((enchantment, level) in enchantments) {
            // 处理 enchantment effect component: koish:attributes
            for (effect in enchantment.getEffects(EnchantmentEffectComponentsPatch.ATTRIBUTES)) {
                action(level, player, slot, effect)
            }

            // 处理 enchantment effect component: koish:abilities
            //
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
        val user = player.toUser()
        val kizamiMap = user.kizamiMap
        kizamiMap.removeAllEffects()
    }

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyKizamiAmount(player, nekoStack) { kizamiMap, kizamizSet -> kizamiMap.subtractOneEach(kizamizSet) }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyKizamiAmount(player, nekoStack) { kizamiMap, kizamiSet -> kizamiMap.addOneEach(kizamiSet) }
    }

    // 基于当前铭刻数量, 将新的铭刻效果应用到玩家身上.
    override fun onEnd(player: Player) {
        val user = player.toUser()
        val kizamiMap = user.kizamiMap
        kizamiMap.applyAllEffects()
    }

    private fun modifyKizamiAmount(player: Player, nekoStack: NekoStack?, update: (KizamiMap, Set<RegistryEntry<KizamiType>>) -> Unit) {
        val kizamiSet = nekoStack?.getKizamiz() ?: return
        val kizamiMap = player.toUser().kizamiMap
        update(kizamiMap, kizamiSet)
    }

    private fun NekoStack.getKizamiz(): Set<RegistryEntry<KizamiType>>? {
        return components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz
    }
}

/**
 * 技能.
 *
 * 物品发生变化时, 根据物品技能, 修改玩家可执行的 [Ability].
 */
internal object AbilityItemSlotChangeListener : ItemSlotChangeEventListener() {

    override val predicates: List<(Player, ItemSlot, ItemStack, NekoStack?) -> Boolean> = listOf(
        ::testSlot,
        ::testLevel,
        ::testDurability
    )

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        // do nothing
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        if (nekoStack == null) return
        val abilities = nekoStack.playerAbilities.takeUnlessEmpty() ?: return
        abilities.forEach { ability -> recordAbility(player, ability, slot to nekoStack) }
    }

    override fun onEnd(player: Player) {
        // 清空技能状态.
        val user = player.toUser()
        user.abilityState.reset()
    }

    private fun recordAbility(player: Player, ability: PlayerAbility, holdBy: Pair<ItemSlot, NekoStack>?) {
        ability.recordBy(CasterAdapter.adapt(player), null, holdBy)
    }
}