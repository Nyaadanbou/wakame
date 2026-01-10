package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.damage.FinalDamageContext
import cc.mewcraft.wakame.entity.player.koishLevel
import cc.mewcraft.wakame.item.extension.level
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.koishItem
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.item.property.impl.ItemSlotGroup
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.isDamageable
import cc.mewcraft.wakame.util.item.maxDamage
import cc.mewcraft.wakame.util.item.toNMS
import org.bukkit.World
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ItemBehaviorContext {
    val player: Player
    val itemstack: ItemStack
}

data class CauseDamageContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val damagee: Entity,
    val finalDamageContext: FinalDamageContext,
) : ItemBehaviorContext {
    val world: World
        get() = player.world

    // 2025/9/14 芙兰
    // 需要用到更多方便的get方法的话, 自行在下方补充即可
    val finalDamage: Double
        get() = finalDamageContext.finalDamageMap.values.sum()
}

data class ReceiveDamageContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val damageSource: DamageSource,
    val finalDamageContext: FinalDamageContext,
) : ItemBehaviorContext {
    val world: World
        get() = player.world

    // 2025/9/14 芙兰
    // 需要用到更多方便的get方法的话, 自行在下方补充即可
    val finalDamage: Double
        get() = finalDamageContext.finalDamageMap.values.sum()
}

data class DurabilityDecreaseContext(
    override val player: Player,
    override val itemstack: ItemStack,
    /**
     * 物品失去的耐久度的原始值.
     * 对应 [org.bukkit.event.player.PlayerItemDamageEvent.damage].
     */
    val originalDurabilityDecreaseValue: Int,
    /**
     * 物品在计算耐久附魔之前, 将要失去的耐久度.
     * 此值只读, 切勿与 [originalDurabilityDecreaseValue] 混淆.
     */
    val durabilityDecreaseValueBeforeEnchantment: Int,
) : ItemBehaviorContext {
    val world: World
        get() = player.world

    var newDurabilityDecreaseValue: Int = 0
    var willModifyDurabilityDecreaseValue: Boolean = false

    /**
     * 修改该物品失去的耐久度.
     */
    fun modifyDurabilityDecreaseValue(value: Int) {
        willModifyDurabilityDecreaseValue = true
        newDurabilityDecreaseValue = value
    }
}

data class StopUseContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val ticksHeldFor: Int,
) : ItemBehaviorContext {
    val world: World
        get() = player.world
}

data class ConsumeContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val hand: InteractionHand,
    /**
     * 物品消耗后返还的物品的原始值.
     * 对应 [org.bukkit.event.player.PlayerItemConsumeEvent.replacement].
     */
    val originalReplacement: ItemStack?,
) : ItemBehaviorContext {
    val world: World
        get() = player.world

    var newReplacement: ItemStack? = null
    var willModifyReplacement: Boolean = false

    /**
     * 修改该物品消耗后返还的物品.
     */
    fun modifyReplacement(itemStack: ItemStack?) {
        willModifyReplacement = true
        newReplacement = itemStack
    }
}

enum class BehaviorResult {
    /**
     * 物品行为执行完毕.
     * 此时代码中断, 不会再执行后续的任何同签名的 handle 函数.
     * 此时不取消相关事件.
     */
    FINISH,

    /**
     * 物品行为执行完毕.
     * 此时代码中断, 不会再执行后续的任何同签名的 handle 函数.
     * 此时取消相关事件.
     */
    FINISH_AND_CANCEL,

    /**
     * 跳过此次物品行为.
     * 此时仍然会继续尝试执行后续的同签名的 handle 函数.
     */
    PASS
}

/**
 * 用于检查一个物品堆叠是否满足“激活”的条件.
 * 例如: 物品是否处于有效的槽位, 物品是否已损坏等.
 */
object ItemStackActivationChecker {
    /**
     * 方便函数.
     * 进行所有必要的检查.
     */
    fun ItemStack.isActive(slot: ItemSlot, player: Player): Boolean {
        return isInValidSlot(slot) && isPlayerLevelEnough(player) && isNotBroken()
    }

    /**
     * 检查物品是否处于有效的槽位.
     */
    fun ItemStack.isInValidSlot(slot: ItemSlot): Boolean {
        if (koishItem == null) return false
        val slotGroup = getProp(ItemPropTypes.SLOT) ?: ItemSlotGroup.empty()
        return slotGroup.contains(slot)
    }

    /**
     * 检查玩家的冒险等级是否满足物品的等级需求.
     */
    fun ItemStack.isPlayerLevelEnough(player: Player): Boolean {
        // 如果不是萌芽物品, 那么认为玩家的等级足够
        if (koishItem == null) return true
        // 如果物品没有等级, 那么认为玩家的等级足够
        val itemLevel = this.level?.level ?: return true

        val playerLevel = player.koishLevel
        return itemLevel <= playerLevel
    }

    /**
     * 检查物品是否不处于损坏状态.
     * @see cc.mewcraft.wakame.item.behavior.impl.HoldLastDamage
     */
    fun ItemStack.isNotBroken(): Boolean {
        // 如果物品有“无法破坏”或耐久组件不完整, 那么认为物品没有耐久度, 应该返回 true
        if (!isDamageable) return true

        // 如果物品损伤值等于最大耐久度, 那么认为物品损坏了
        // HoldLastDamage 物品行为存在时, 会使物品耐久度最低维持在0
        // 此时物品损伤值等于最大耐久度, 该函数返回 false, 符合预期逻辑
        return damage < maxDamage
    }
}

// ------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的 ItemBehavior
// ------------

/**
 * 当判断一个物品是否拥有特定的 [ItemBehavior] 时 (类型绝对匹配, 不包含子类型), 尽量使用这个函数, 性能比较好.
 */
fun ItemStack.hasBehaviorExact(behavior: ItemBehavior): Boolean = toNMS().hasBehaviorExact(behavior)

inline fun <reified T : ItemBehavior> ItemStack.hasBehavior(): Boolean = toNMS().hasBehavior<T>()

inline fun <reified T : ItemBehavior> ItemStack.getBehavior(): T? = toNMS().getBehavior<T>()

inline fun ItemStack.handleBehavior(action: (ItemBehavior) -> Unit) = toNMS().handleBehavior(action)

// ------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的 ItemBehavior
// ------------

fun MojangStack.hasBehaviorExact(behavior: ItemBehavior): Boolean =
    koishItem?.behaviors?.hasExact(behavior) == true

inline fun <reified T : ItemBehavior> MojangStack.hasBehavior(): Boolean =
    koishItem?.behaviors?.has(T::class) == true

inline fun <reified T : ItemBehavior> MojangStack.getBehavior(): T? =
    koishItem?.behaviors?.get(T::class)

inline fun MojangStack.handleBehavior(action: (ItemBehavior) -> Unit) {
    koishItem?.behaviors?.forEach(action)
}