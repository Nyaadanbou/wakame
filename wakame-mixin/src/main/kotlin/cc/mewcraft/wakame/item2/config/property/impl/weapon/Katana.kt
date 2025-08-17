package cc.mewcraft.wakame.item2.config.property.impl.weapon

import org.joml.Vector3f
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 太刀的配置.
 *
 * @property itemDamagePerAttack 每次攻击命中消耗的耐久度.
 * @property unarmedSpiritConsume 玩家非手持太刀时, 每秒气刃值减少量.
 * @property allowComboTicks 太刀连段允许的时间.
 * @property horizontalSlashHalfExtentsBase 横斩OBB攻击判定范围.
 * @property horizontalSlashDamageMultiplier 横斩伤害修饰值.
 * @property horizontalSlashCooldown 横斩后物品冷却.
 * @property horizontalSlashSpiritReward 横斩命中奖励的气刃值.
 * @property spiritBladeSlashHalfExtentsBase 气刃斩OBB攻击判定范围.
 * @property spiritBladeSlashDamageMultiplier1 气刃斩1伤害修饰值.
 * @property spiritBladeSlashDamageMultiplier2 气刃斩2伤害修饰值.
 * @property spiritBladeSlashDamageMultiplier3 气刃斩3伤害修饰值.
 * @property spiritBladeSlashSpiritConsume1 发动气刃斩1所需的气刃值.
 * @property spiritBladeSlashSpiritConsume2 发动气刃斩2所需的气刃值.
 * @property spiritBladeSlashSpiritConsume3 发动气刃斩3所需的气刃值.
 * @property spiritBladeSlashCooldown1 气刃斩1后物品冷却.
 * @property spiritBladeSlashCooldown2 气刃斩2后物品冷却.
 * @property spiritBladeSlashCooldown3 气刃斩3后物品冷却.
 * @property roundSlashRadius 回旋斩半径.
 * @property roundSlashDamageMultiplier 回旋斩伤害修饰值.
 * @property roundSlashSpiritConsume 发动回旋斩所需的气刃值.
 * @property roundSlashCooldown 回旋斩后物品冷却.
 * @property foresightSlashSpiritRequire 发动看破斩所需的气刃值.
 * @property foresightSlashVelocityMultiplier 看破斩位移速度倍率值.
 * @property foresightSlashHalfExtentsBase 看破斩OBB攻击判定范围.
 * @property foresightSlashDamageMultiplier 看破斩伤害修饰值.
 * @property weakForesightSlashDamageMultiplier 气刃值不足时看破斩伤害修饰值.
 * @property foresightSlashCooldown 看破斩后物品冷却.
 * @property weakForesightSlashCooldown 气刃值不足时看破斩后物品冷却.
 * @property foresightSlashDurationTicks 看破斩无敌持续时间.
 * @property weakForesightSlashDurationTicks 气刃值不足时看破斩无敌持续时间.
 * @property foresightSlashSpiritReward 看破斩判定成功奖励的气刃值.
 * @property weakForesightSlashSpiritReward 气刃值不足时看破斩判定成功奖励的气刃值.
 */
@ConfigSerializable
data class Katana(
    val itemDamagePerAttack: Int = 1,
    val unarmedSpiritConsume: Int = 5,
    val allowComboTicks: Int = 12,

    val horizontalSlashHalfExtentsBase: Vector3f = Vector3f(1.2f, 0.05f, 1.1f),
    val horizontalSlashDamageMultiplier: Double = 1.0,
    val horizontalSlashCooldown: Int = 12,
    val horizontalSlashSpiritReward: Int = 4,

    val spiritBladeSlashHalfExtentsBase: Vector3f = Vector3f(1.7f, 0.05f, 1.4f),
    val spiritBladeSlashDamageMultiplier1: Double = 1.15,
    val spiritBladeSlashDamageMultiplier2: Double = 1.2,
    val spiritBladeSlashDamageMultiplier3: Double = 1.35,
    val spiritBladeSlashSpiritConsume1: Int = 15,
    val spiritBladeSlashSpiritConsume2: Int = 15,
    val spiritBladeSlashSpiritConsume3: Int = 20,
    val spiritBladeSlashCooldown1: Int = 10,
    val spiritBladeSlashCooldown2: Int = 10,
    val spiritBladeSlashCooldown3: Int = 12,

    val roundSlashRadius: Double = 3.5,
    val roundSlashDamageMultiplier: Double = 1.5,
    val roundSlashSpiritConsume: Int = 20,
    val roundSlashCooldown: Int = 20,

    val foresightSlashSpiritRequire: Int = 10,
    val foresightSlashVelocityMultiplier: Double = 0.8,
    val foresightSlashHalfExtentsBase: Vector3f = Vector3f(1.2f, 0.05f, 1.4f),
    val foresightSlashDamageMultiplier: Double = 1.15,
    val weakForesightSlashDamageMultiplier: Double = 0.5,
    val foresightSlashCooldown: Int = 14,
    val weakForesightSlashCooldown: Int = 14,
    val foresightSlashDurationTicks: Int = 12,
    val weakForesightSlashDurationTicks: Int = 4,
    val foresightSlashSpiritReward: Int = 100,
    val weakForesightSlashSpiritReward: Int = 100,
)
