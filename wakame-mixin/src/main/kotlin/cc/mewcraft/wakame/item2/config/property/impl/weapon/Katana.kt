package cc.mewcraft.wakame.item2.config.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 太刀的配置.
 *
 * @property unarmedSpiritConsume 玩家非手持太刀时, 每秒气刃值减少量.
 * @property horizontalSlashCooldown 横斩后物品冷却.
 * @property horizontalSlashSpiritReward 横斩命中奖励的气刃值.
 * @property spiritBladeSlashSpiritConsume1 发动气刃斩1所需的气刃值.
 * @property spiritBladeSlashSpiritConsume2 发动气刃斩2所需的气刃值.
 * @property spiritBladeSlashSpiritConsume3 发动气刃斩3所需的气刃值.
 * @property spiritBladeSlashCooldown1 气刃斩1后物品冷却.
 * @property spiritBladeSlashCooldown2 气刃斩2后物品冷却.
 * @property spiritBladeSlashCooldown3 气刃斩3后物品冷却.
 * @property spiritBladeSlashAllowComboTicks 气刃斩连段允许的时间.
 * @property laiSlashSpiritConsume 发动正常居合斩所需的气刃值.
 * @property laiSlashCooldown 居合斩后物品冷却.
 * @property laiSlashTicks 发动正常居合斩给予的无敌 tick 数.
 * @property weakLaiSlashTicks 气刃值不足时发动居合斩给予的无敌 tick 数.
 * @property laiSlashSpiritReward 居合斩命中奖励的气刃值.
 * @property laiSlashVelocityMultiply 居合斩位移速度倍率.
 * @property roundSlashRadius 回旋斩半径.
 * @property roundSlashSpiritConsume 发动回旋斩所需的气刃值.
 * @property roundSlashCooldown 回旋斩后物品冷却.
 * @property dragonAscendSlashSpiritConsume 发动登龙斩所需的气刃值.
 * @property dragonAscendSlashCooldown 登龙斩后物品冷却.
 */
@ConfigSerializable
data class Katana(
    val unarmedSpiritConsume: Int = 5,
    val horizontalSlashCooldown: Int = 14,
    val horizontalSlashSpiritReward: Int = 4,
    val spiritBladeSlashSpiritConsume1: Int = 15,
    val spiritBladeSlashSpiritConsume2: Int = 15,
    val spiritBladeSlashSpiritConsume3: Int = 20,
    val spiritBladeSlashCooldown1: Int = 10,
    val spiritBladeSlashCooldown2: Int = 10,
    val spiritBladeSlashCooldown3: Int = 12,
    val spiritBladeSlashAllowComboTicks: Int = 40,
    val laiSlashSpiritConsume: Int = 10,
    val laiSlashCooldown: Int = 20,
    val laiSlashTicks: Int = 15,
    val weakLaiSlashTicks: Int = 5,
    val laiSlashSpiritReward: Int = 50,
    val laiSlashVelocityMultiply: Double = 3.0,
    val roundSlashRadius: Double = 3.5,
    val roundSlashSpiritConsume: Int = 20,
    val roundSlashCooldown: Int = 20,
    val dragonAscendSlashSpiritConsume: Int = 25,
    val dragonAscendSlashCooldown: Int = 40,
)
