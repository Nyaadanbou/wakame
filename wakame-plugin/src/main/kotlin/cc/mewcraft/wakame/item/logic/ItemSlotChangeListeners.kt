package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.attribute.AttributeInstance
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.enchantment.CustomEnchantment
import cc.mewcraft.wakame.enchantment.customEnchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry.getBy
import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 攻击速度.
 *
 * 物品发生变化时, 根据物品攻击速度, 更新所有必要的状态.
 */
internal object AttackSpeedItemSlotChangeListener : ItemSlotChangeListener() {
    override fun test(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        return testSlot(player, slot, itemStack, nekoStack) &&
                testLevel(player, slot, itemStack, nekoStack) &&
                testDurability(player, slot, itemStack, nekoStack)
    }

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        nekoStack?.handleAttackSpeed { /* TODO 完善攻击速度 */ }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        nekoStack?.handleAttackSpeed { /* TODO 完善攻击速度 */ }
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
internal object AttributeItemSlotChangeListener : ItemSlotChangeListener() {
    override fun test(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        return testSlot(player, slot, itemStack, nekoStack) &&
                testLevel(player, slot, itemStack, nekoStack) &&
                testDurability(player, slot, itemStack, nekoStack)
    }

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
internal object EnchantmentItemSlotChangeListener : ItemSlotChangeListener() {
    // 在 1.20.5 以后, 可以通过数据包添加自定义的魔咒.
    // 这也意味着我们不再需要手动处理与附魔相关的机制, 例如铁砧.
    // 唯一需要处理的就是监听物品栏发生的变化, 以应用附魔的效果.

    override fun test(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        return testLevel(player, slot, itemStack, nekoStack) &&
                testDurability(player, slot, itemStack, nekoStack)
    }

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyEnchantmentEffects(player, slot, itemStack) { effect, user -> effect.removeFrom(user) }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyEnchantmentEffects(player, slot, itemStack) { effect, user -> effect.applyTo(user) }
    }

    private fun modifyEnchantmentEffects(player: Player, slot: ItemSlot, itemStack: ItemStack, update: (EnchantmentEffect, User<Player>) -> Unit) {
        val user = player.toUser()
        val customEnchantments = itemStack.customEnchantments
        for ((enchantment, level) in customEnchantments) {
            if (!testEnchantmentSlot(slot, enchantment)) {
                // 附魔是否生效, 取决于附魔(nms)本身的 slots 设置,
                // 但这样就无法支持原版之外的 slot (例如让饰品附魔生效).
                // 到底该设计成什么样还有待进一步讨论???
                continue
            }
            for (effect in enchantment.getEffects(level, slot)) {
                update(effect, user)
            }
        }
    }

    private fun testEnchantmentSlot(slot: ItemSlot, enchantment: CustomEnchantment): Boolean {
        return slot.testEquipmentSlotGroup(enchantment.handle.activeSlotGroups)
    }
}

/**
 * 铭刻.
 *
 * 物品发生变化时, 根据物品铭刻, 修改玩家的 [cc.mewcraft.wakame.kizami.KizamiMap].
 */
// TODO 重构 Kizami 的数据结构和更新逻辑
//  灵感: 尝试在 Kizami 中引入一个标记 dirty, 表示是否需要更新 effects.
//  当 dirty 为 true 时, 在下一次 tick 时, 重新计算 effects.
//  否则, 下次 tick 不需要动这个 Kizami.
internal object KizamiItemSlotChangeListener : ItemSlotChangeListener() {
    override fun test(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        return testSlot(player, slot, itemStack, nekoStack) &&
                testLevel(player, slot, itemStack, nekoStack) &&
                testDurability(player, slot, itemStack, nekoStack)
    }

    // 首先, 从玩家身上移除所有已有的铭刻效果.
    // 我们将重新计算铭刻数量, 并将新的铭刻效果
    // (基于新的铭刻数量)应用到玩家身上.
    override fun onBegin(player: Player) {
        val user = player.toUser()
        val kizamiMap = user.kizamiMap
        for ((kizami, amount) in kizamiMap) {
            KizamiRegistry.EFFECTS.getBy(kizami, amount).remove(user)
        }
    }

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyKizamiAmount(player, nekoStack) { kizamiMap, kizamiz -> kizamiMap.subtractOneEach(kizamiz) }
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        modifyKizamiAmount(player, nekoStack) { kizamiMap, kizamiz -> kizamiMap.addOneEach(kizamiz) }
    }

    // 基于当前铭刻数量, 将新的铭刻效果应用到玩家身上.
    override fun onEnd(player: Player) {
        val user = player.toUser()
        val kizamiMap = user.kizamiMap
        val iterator = kizamiMap.iterator()
        while (iterator.hasNext()) {
            val (kizami, amount) = iterator.next()
            if (amount > 0) {
                KizamiRegistry.EFFECTS.getBy(kizami, amount).apply(user)
            } else {
                iterator.remove()
            }
        }
    }

    private fun modifyKizamiAmount(player: Player, nekoStack: NekoStack?, update: (KizamiMap, Set<Kizami>) -> Unit) {
        val kizamiz = nekoStack?.getKizamiz() ?: return
        val kizamiMap = player.toUser().kizamiMap
        update(kizamiMap, kizamiz)
    }

    private fun NekoStack.getKizamiz(): Set<Kizami>? {
        return components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz
    }
}

/**
 * 技能.
 *
 * 物品发生变化时, 根据物品技能, 修改玩家可执行的 [Ability].
 */
internal object AbilityItemSlotChangeListener : ItemSlotChangeListener() {
    override fun test(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        return testSlot(player, slot, itemStack, nekoStack) &&
                testLevel(player, slot, itemStack, nekoStack) &&
                testDurability(player, slot, itemStack, nekoStack)
    }

    override fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        // do nothing
    }

    override fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?) {
        nekoStack ?: return
        val abilities = nekoStack.getAbilities() ?: return
        abilities.forEach { ability -> recordAbility(player, ability, slot to nekoStack) }
    }

    override fun onEnd(player: Player) {
        // 清空技能状态.
        val user = player.toUser()
        user.abilityState.reset()
    }

    private fun NekoStack.getAbilities(): Collection<PlayerAbility>? {
        val cells = components.get(ItemComponentTypes.CELLS) ?: return null
        // FIXME 这里有潜在 BUG, 详见: https://github.com/Nyaadanbou/wakame/issues/132
        val abilities = cells.collectAbilityModifiers(this, ItemSlot.imaginary())
        return abilities
    }

    private fun recordAbility(player: Player, ability: PlayerAbility, holdBy: Pair<ItemSlot, NekoStack>?) {
        ability.recordBy(CasterAdapter.adapt(player), null, holdBy)
    }
}