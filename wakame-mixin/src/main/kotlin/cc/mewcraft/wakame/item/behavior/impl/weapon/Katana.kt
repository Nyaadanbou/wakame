package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.optionalEntry
import cc.mewcraft.wakame.damage.KoishDamageSources
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.behavior.impl.weapon.WeaponUtils.getInputDirection
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.damageItem
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.weapon.Katana
import cc.mewcraft.wakame.util.adventure.SoundSource
import cc.mewcraft.wakame.util.adventure.playSound
import cc.mewcraft.wakame.util.runTaskLater
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.papermc.paper.registry.keys.SoundEventKeys
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import xyz.xenondevs.commons.provider.orElse
import kotlin.random.Random

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

/**
 * 太刀的物品行为.
 *
 * 该 `object` 里的所有逻辑都是与事件相关的, 不包含 tick 逻辑.
 * tick 逻辑全部由 [SwitchKatana] 和 [TickKatana] 实现.
 */
object Katana : Weapon {

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemStack = context.itemstack
        val player = context.player
        val katanaState = player.koishify().getOrNull(KatanaState) ?: return InteractionResult.FAIL
        if (katanaState.isArmed.not()) return InteractionResult.FAIL
        if (itemStack.isOnCooldown(player)) return InteractionResult.FAIL

        val currentAction = katanaState.currentAction
        when (currentAction) {
            KatanaState.ActionType.IDLE,
            KatanaState.ActionType.HORIZONTAL_SLASH,
            KatanaState.ActionType.FORESIGHT_SLASH,
                -> {
                if (player.isSneaking) {
                    // 能够发动气刃斩1
                    if (katanaState.canUseSpiritBladeSlash1()) {
                        spiritBladeSlash1(player, itemStack, katanaState)
                        return InteractionResult.SUCCESS
                    }
                }
            }

            KatanaState.ActionType.SPIRIT_BLADE_SLASH_1 -> {
                if (player.isSneaking) {
                    // 能够发动气刃斩2
                    if (katanaState.canUseSpiritBladeSlash2()) {
                        spiritBladeSlash2(player, itemStack, katanaState)
                        return InteractionResult.SUCCESS
                    }
                }
            }

            KatanaState.ActionType.SPIRIT_BLADE_SLASH_2 -> {
                if (player.isSneaking) {
                    // 能够发动气刃斩3
                    if (katanaState.canUseSpiritBladeSlash3()) {
                        spiritBladeSlash3(player, itemStack, katanaState)
                        return InteractionResult.SUCCESS
                    }
                }
            }

            KatanaState.ActionType.SPIRIT_BLADE_SLASH_3 -> {
                if (player.isSneaking) {
                    // 能够发动气刃大回旋斩
                    if (katanaState.canUseRoundSlash()) {
                        roundSlash(player, itemStack, katanaState)
                        return InteractionResult.SUCCESS
                    }
                }
            }

            KatanaState.ActionType.ROUND_SLASH -> {
                // 气刃大回旋斩之后只能横斩
            }

            // KatanaState.ActionType.DRAGON_ASCEND_SLASH -> {}
        }
        // 如果前面的状态都没有 return
        // 则发动发动横斩
        horizontalSlash(player, itemStack, katanaState)
        return InteractionResult.SUCCESS
    }

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemStack = context.itemstack
        val player = context.player
        if (context.hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL

        val katanaState = player.koishify().getOrNull(KatanaState) ?: return InteractionResult.FAIL
        if (katanaState.isArmed.not()) return InteractionResult.FAIL
        if (itemStack.isOnCooldown(player)) return InteractionResult.FAIL

        val currentAction = katanaState.currentAction
        val config = katanaState.config
        when (currentAction) {
            KatanaState.ActionType.ROUND_SLASH,
            KatanaState.ActionType.FORESIGHT_SLASH,
                -> {
                // 气刃大回旋斩之后不能看破斩
                // 看破斩之后不能看破斩(不能复读)
                return InteractionResult.FAIL
            }

            KatanaState.ActionType.IDLE,
            KatanaState.ActionType.HORIZONTAL_SLASH,
            KatanaState.ActionType.SPIRIT_BLADE_SLASH_1,
            KatanaState.ActionType.SPIRIT_BLADE_SLASH_2,
            KatanaState.ActionType.SPIRIT_BLADE_SLASH_3,
                -> {
                return if (katanaState.currentBladeSpirit >= config.foresightSlashSpiritRequire) {
                    // 气刃值足够发动正常看破斩
                    foresightSlash(player, itemStack, katanaState)
                    InteractionResult.SUCCESS
                } else {
                    // 气刃值不足以发动正常看破斩
                    weakForesightSlash(player, itemStack, katanaState)
                    InteractionResult.SUCCESS
                }
            }

            // KatanaState.ActionType.DRAGON_ASCEND_SLASH -> {}
        }
    }

    override fun handleReceiveDamage(context: ReceiveDamageContext): BehaviorResult {
        val player = context.player
        val katanaState = player.koishify().getOrNull(KatanaState) ?: return BehaviorResult.PASS
        if (katanaState.isArmed.not()) return BehaviorResult.PASS

        return foresightSlashCheck(player, katanaState, context)
    }

    /**
     * 执行太刀横斩的攻击效果.
     */
    private fun horizontalSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        val damageMetadata = PlayerDamageMetadata(player.attributeContainer) {
            every {
                standard()
                rate { // override
                    katanaState.getBladeLevelDamageRate() * config.horizontalSlashDamageMultiplier * standard()
                }
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 5.0, config.horizontalSlashHalfExtentsBase)
        val damageSource = KoishDamageSources.playerAttack(player)
        // 造成伤害
        val flag = hitEntities.any { entity ->
            entity.hurt(damageMetadata, damageSource, true)
        }
        // 如果成功造成了伤害
        if (flag) {
            // 增加气刃值
            katanaState.addBladeSpirit(config.horizontalSlashSpiritReward)
            // 设置耐久
            player.damageItem(EquipmentSlot.HAND, config.itemDamagePerAttack)
        }
        // 设置冷却
        val cooldown = config.horizontalSlashCooldown
        itemstack.addCooldown(player, cooldown)
        // 连招状态
        katanaState.changeAction(KatanaState.ActionType.HORIZONTAL_SLASH, cooldown + config.allowComboTicks)
        if (katanaState.canUseSpiritBladeSlash1()) {
            // TODO 显然这个文本不应该写死, 先临时这样, 下同
            player.sendActionBar(Component.text("左键: 横斩   潜行+左键: 左气刃斩   右键: 看破斩"))
        } else {
            player.sendActionBar(Component.text("左键: 横斩   右键: 看破斩"))
        }
    }

    /**
     * 方便函数.
     * 执行太刀气刃斩的通用攻击效果.
     */
    private fun spiritBladeSlashBase(
        player: Player, katanaState: KatanaState, damageMultiplier: Double, angel: Float,
    ) {
        val config = katanaState.config
        val damageMetadata = PlayerDamageMetadata(player.attributeContainer) {
            every {
                standard()
                rate { // override
                    katanaState.getBladeLevelDamageRate() * damageMultiplier * standard()
                }
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 5.0, config.spiritBladeSlashHalfExtentsBase, angel)
        val damageSource = KoishDamageSources.playerAttack(player)
        // 造成伤害
        val flag = hitEntities.any { entity ->
            entity.hurt(damageMetadata, damageSource, true)
        }
        // 如果成功造成了伤害
        if (flag) {
            // 设置耐久
            player.damageItem(EquipmentSlot.HAND, config.itemDamagePerAttack)
        }
    }

    /**
     * 执行太刀气刃斩1(左气刃斩)的攻击效果.
     */
    private fun spiritBladeSlash1(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-config.spiritBladeSlashSpiritConsume1)
        spiritBladeSlashBase(player, katanaState, config.spiritBladeSlashDamageMultiplier1, Random.nextDouble(32.0, 38.0).toFloat())
        // 设置冷却
        val cooldown = config.spiritBladeSlashCooldown1
        itemstack.addCooldown(player, cooldown)
        // 连招状态
        katanaState.changeAction(KatanaState.ActionType.SPIRIT_BLADE_SLASH_1, cooldown + config.allowComboTicks)
        if (katanaState.canUseSpiritBladeSlash2()) {
            player.sendActionBar(Component.text("左键: 横斩   潜行+左键: 右气刃斩   右键: 看破斩"))
        } else {
            player.sendActionBar(Component.text("左键: 横斩   右键: 看破斩"))
        }
    }

    /**
     * 执行太刀气刃斩2(右气刃斩)的攻击效果.
     */
    private fun spiritBladeSlash2(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-config.spiritBladeSlashSpiritConsume2)
        spiritBladeSlashBase(player, katanaState, config.spiritBladeSlashDamageMultiplier2, Random.nextDouble(-38.0, -32.0).toFloat())
        // 设置冷却
        val cooldown = config.spiritBladeSlashCooldown2
        itemstack.addCooldown(player, cooldown)
        // 连招状态
        katanaState.changeAction(KatanaState.ActionType.SPIRIT_BLADE_SLASH_2, cooldown + config.allowComboTicks)
        if (katanaState.canUseSpiritBladeSlash3()) {
            player.sendActionBar(Component.text("左键: 横斩   潜行+左键: 气刃三连斩   右键: 看破斩"))
        } else {
            player.sendActionBar(Component.text("左键: 横斩   右键: 看破斩"))
        }
    }

    /**
     * 执行太刀气刃斩3(气刃三连斩)的攻击效果.
     */
    private fun spiritBladeSlash3(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-config.spiritBladeSlashSpiritConsume3)
        spiritBladeSlashBase(player, katanaState, config.spiritBladeSlashDamageMultiplier3, Random.nextDouble(32.0, 38.0).toFloat())
        runTaskLater(3) { ->
            spiritBladeSlashBase(player, katanaState, config.spiritBladeSlashDamageMultiplier3, Random.nextDouble(-38.0, -32.0).toFloat())
        }
        runTaskLater(6) { ->
            spiritBladeSlashBase(player, katanaState, config.spiritBladeSlashDamageMultiplier3, Random.nextDouble(85.0, 95.0).toFloat())
        }
        // 设置冷却
        val cooldown = config.spiritBladeSlashCooldown3
        itemstack.addCooldown(player, cooldown)
        // 连招状态
        katanaState.changeAction(KatanaState.ActionType.SPIRIT_BLADE_SLASH_3, cooldown + config.allowComboTicks)
        if (katanaState.canUseRoundSlash()) {
            player.sendActionBar(Component.text("左键: 横斩   潜行+左键: 气刃大回旋斩   右键: 看破斩"))
        } else {
            player.sendActionBar(Component.text("左键: 横斩   右键: 看破斩"))
        }
    }

    /**
     * 执行太刀气刃大回旋斩的攻击效果.
     */
    private fun roundSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-config.roundSlashSpiritConsume)
        // 造成伤害
        val centerLocation = player.location.add(.0, player.boundingBox.height / 2, .0)
        val attributeContainer = player.attributeContainer
        val scale = attributeContainer.getValue(Attributes.SCALE).toFloat()
        val hitEntities = centerLocation.getNearbyLivingEntities(config.roundSlashRadius * scale, 1.1 * scale) { it != player }
        val damageMetadata = PlayerDamageMetadata(attributeContainer) {
            every {
                standard()
                rate {
                    katanaState.getBladeLevelDamageRate() * config.roundSlashDamageMultiplier * standard()
                }
            }
        }
        val damageSource = KoishDamageSources.playerAttack(player)
        // 造成伤害
        val flag = hitEntities.any { entity ->
            entity.hurt(damageMetadata, damageSource, true)
        }
        // 如果成功造成了伤害
        if (flag) {
            // 提升气刃等级
            katanaState.upgradeBladeLevel()
            // 设置耐久
            player.damageItem(EquipmentSlot.HAND, config.itemDamagePerAttack)
            if (LOGGING) player.sendMessage("回旋斩命中! 气刃等级提升!")
        } else {
            if (LOGGING) player.sendMessage("回旋斩未命中!")
        }
        // 设置冷却
        val cooldown = config.roundSlashCooldown
        itemstack.addCooldown(player, cooldown)
        // 连招状态
        katanaState.changeAction(KatanaState.ActionType.ROUND_SLASH, cooldown + config.allowComboTicks)
        player.sendActionBar(Component.text("左键: 横斩"))
    }

    /**
     * 方便函数.
     * 执行太刀看破斩的通用攻击效果.
     */
    private fun foresightSlashBase(
        player: Player, itemstack: ItemStack, katanaState: KatanaState,
        damageMultiplier: Double, durationTicks: Int, cooldown: Int,
    ) {
        val config = katanaState.config
        // 重置标记
        katanaState.isForesightSlashAlreadyReward = false
        katanaState.isForesightSlashHitTarget = false
        // 消耗所有气刃值
        katanaState.addBladeSpirit(-KatanaState.MAX_BLADE_SPIRIT)
        val damageMetadata = PlayerDamageMetadata(player.attributeContainer) {
            every {
                standard()
                rate { // override
                    katanaState.getBladeLevelDamageRate() * damageMultiplier * standard()
                }
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 5.0, config.foresightSlashHalfExtentsBase)
        val damageSource = KoishDamageSources.playerAttack(player)
        // 造成伤害
        val flag = hitEntities.any { entity ->
            entity.hurt(damageMetadata, damageSource, true)
        }
        // 如果成功造成了伤害
        if (flag) {
            // 设置耐久
            player.damageItem(EquipmentSlot.HAND, config.itemDamagePerAttack)
            // 标记命中生物
            katanaState.isForesightSlashHitTarget = true
        }
        // 设置看破斩判定时间
        katanaState.remainingForesightSlashTicks = durationTicks
        // 位移
        val inputDirection = player.getInputDirection()
        val velocity = if (inputDirection == null) {
            // 无输入时默认向后位移
            player.location.direction.setY(0).normalize().multiply(-config.foresightSlashVelocityMultiplier)
        } else {
            // 向玩家输入的方向位移
            Vector(inputDirection.x, inputDirection.y, inputDirection.z).multiply(config.foresightSlashVelocityMultiplier)
        }
        player.velocity = velocity
        // 设置冷却
        itemstack.addCooldown(player, cooldown)
        // 连招状态
        katanaState.changeAction(KatanaState.ActionType.FORESIGHT_SLASH, cooldown + config.allowComboTicks)
        if (katanaState.canUseSpiritBladeSlash1()) {
            player.sendActionBar(Component.text("左键: 横斩   潜行+左键: 左气刃斩"))
        } else {
            player.sendActionBar(Component.text("左键: 横斩"))
        }
    }

    /**
     * 执行太刀看破斩(气刃值足够时)的攻击效果.
     */
    private fun foresightSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        foresightSlashBase(
            player, itemstack, katanaState,
            config.foresightSlashDamageMultiplier, config.foresightSlashDurationTicks, config.foresightSlashCooldown
        )
    }

    /**
     * 执行太刀看破斩(气刃值不足时)的攻击效果.
     */
    private fun weakForesightSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val config = katanaState.config
        foresightSlashBase(
            player, itemstack, katanaState,
            config.weakForesightSlashDamageMultiplier, config.weakForesightSlashDurationTicks, config.weakForesightSlashCooldown
        )
    }

    /**
     * 检查玩家受伤时是否满足看破斩成功条件.
     * 若满足成功条件则给予奖励.
     */
    private fun foresightSlashCheck(player: Player, katanaState: KatanaState, context: ReceiveDamageContext): BehaviorResult {
        val config = katanaState.config
        // 不存在伤害来源实体 - 不处理
        context.damageSource.causingEntity as? LivingEntity ?: return BehaviorResult.PASS
        // 玩家不处于看破斩判定时间内 - 不处理
        if (katanaState.remainingForesightSlashTicks <= 0) return BehaviorResult.PASS

        // 判定看破斩奖励
        // 前提: 玩家在看破斩期间受伤(执行到这里说明已满足该条件)
        // 看破斩命中生物 且 看破斩奖励未给予, 才奖励气刃值
        if (katanaState.isForesightSlashHitTarget && !katanaState.isForesightSlashAlreadyReward) {
            // 修改标记
            katanaState.isForesightSlashAlreadyReward = true
            // 加气刃值
            katanaState.addBladeSpirit(config.foresightSlashSpiritReward)
            // 播放音效 // TODO 音效和特效: 叮~
            player.playSound(player) {
                type(SoundEventKeys.ENTITY_EXPERIENCE_ORB_PICKUP)
                source(SoundSource.PLAYER)
            }
        }

        // 处于无敌时间都取消伤害
        // 即使看破斩奖励判定未成功
        return BehaviorResult.FINISH_AND_CANCEL
    }


    /**
     * 太刀气刃登龙斩.
     * TODO
     */
    private fun dragonAscendSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config

        if (LOGGING) player.sendMessage("登龙斩!")

        // 消耗气和一层气刃等级
        // katanaState.addBladeSpirit(-katanaConfig.dragonAscendSlashSpiritConsume)
        katanaState.downgradeBladeLevel()

        // 登龙效果

        // 设置冷却
        // itemstack.addCooldown(player, katanaConfig.dragonAscendSlashCooldown)
        // 设置耐久
        player.damageItem(EquipmentSlot.HAND, katanaConfig.itemDamagePerAttack)
    }
}

/**
 * 切换手持太刀时的逻辑.
 *
 * 这同时也是一个标准的“监听”物品变化来执行逻辑的写法.
 */
object SwitchKatana : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        val katanaState = entity.getOrNull(KatanaState)

        // 99% 的情况应该只需要关注当前 tick 发生了变化的物品. 无变化的物品一般不需要关注.
        // 所以可以直接使用这个函数来快速遍历所有在当前 tick 发生了变化的物品.
        // 其中 slot 是物品槽位, curr 是新的物品, prev 是旧的物品.
        slotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null &&
                ItemSlotChanges.testSlot(slot, prev)
            ) {
                val katanaItem = prev.getProp(ItemPropTypes.KATANA)
                if (katanaItem != null && katanaState != null) {
                    katanaState.isArmed = false
                }
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val katanaConfig = curr.getProp(ItemPropTypes.KATANA)
                if (katanaConfig != null) {
                    if (katanaState != null) {
                        katanaState.config = katanaConfig // 始终切换至新的太刀配置
                        katanaState.isArmed = true
                        katanaState.unarmedTicks = 0
                    } else {
                        entity.configure {
                            it += KatanaState(katanaConfig)
                        }
                    }
                }
            }
        }
    }
}

// TODO #349: 也许可以把这部分逻辑放到 ItemBehavior 里, 但会存在比较大的局限性.
//  相当于只有当物品[有效]时 tick 逻辑才能执行. 与[有效]相反的[无效]情况就包括了玩家把物品丢地上或者放箱子里,
//  此时 ItemBehavior 是无法执行这个所谓的 tick 函数的, 因为我们无法轻易从这个已经离开玩家的物品上获取到除了物品本身以外的信息 (如玩家).
//  这时候如果 tick 逻辑需要玩家则只能在构建一个 tick 任务时将玩家信息传递进去, 而这个任务一般都是一个追踪起来比较丑陋的 BukkitTask.
//  而 Fleks 可以实现同样的效果, 并且也可以更清晰的管理跟 tick 相关的所有状态, 所以写个 Fleks system 从各方面来说都更好.
/**
 * tick 太刀状态的逻辑.
 */
object TickKatana : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, KatanaState) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val katanaState = entity[KatanaState]
        if (!katanaState.isArmed) {
            // 未手持太刀时, 气刃值自动降低
            if (katanaState.unarmedTicks++ >= 20) {
                katanaState.unarmedTicks = 0
                katanaState.addBladeSpirit(-katanaState.config.unarmedSpiritConsume)
                // 如果玩家的气刃值为0, 且气刃等级为无刃, 则不再记录数据, 也不再 tick
                if (katanaState.currentBladeSpirit <= 0 && katanaState.currentBladeLevel == KatanaState.BladeLevel.NONE) {
                    katanaState.bossBar.removeViewer(player)
                    entity.configure { it -= KatanaState }
                    return
                }
            }
        }

        // 气刃等级持续时间自减和降级
        if (katanaState.remainingBladeLevelTicks > 0) {
            katanaState.remainingBladeLevelTicks -= 1
            if (katanaState.remainingBladeLevelTicks == 0) {
                katanaState.downgradeBladeLevel()
            }
        }

        // 连招时间自减和回到待机状态
        if (katanaState.currentAction != KatanaState.ActionType.IDLE && katanaState.remainingComboTicks > 0) {
            katanaState.remainingComboTicks -= 1
            if (katanaState.remainingComboTicks <= 0) {
                katanaState.changeAction(KatanaState.ActionType.IDLE, 0)
                player.sendActionBar(Component.text("左键: 横斩   右键: 看破斩"))
            }
        }

        // 无敌时间自减
        if (katanaState.remainingForesightSlashTicks > 0) {
            katanaState.remainingForesightSlashTicks -= 1
        }

        // 更新 BossBar
        val bossBar = katanaState.bossBar
        val progress = katanaState.currentBladeSpirit.toFloat() / KatanaState.MAX_BLADE_SPIRIT
        val text = Component.text("气刃值 ${katanaState.currentBladeSpirit} / ${KatanaState.MAX_BLADE_SPIRIT}")
        val color = when (katanaState.currentBladeLevel) {
            KatanaState.BladeLevel.NONE -> BossBar.Color.BLUE
            KatanaState.BladeLevel.WHITE -> BossBar.Color.WHITE
            KatanaState.BladeLevel.YELLOW -> BossBar.Color.YELLOW
            KatanaState.BladeLevel.RED -> BossBar.Color.RED
        }
        bossBar.name(text)
        bossBar.progress(progress)
        bossBar.color(color)
        bossBar.addViewer(player)
    }
}

/**
 * 用于记录玩家使用太刀时的相关数据.
 *
 * 该类型会在玩家使用太刀时实例化/更新.
 *
 * @property isArmed 玩家当前是否手持太刀.
 * @property unarmedTicks 玩家非手持太刀累计时间.
 * @property isForesightSlashHitTarget 标记看破斩是否命中生物.
 * @property isForesightSlashAlreadyReward 标记看破斩奖励是否已经给予.
 * @property remainingForesightSlashTicks 看破斩的剩余判定时间.
 * @property remainingBladeLevelTicks 当前气刃等级的剩余持续时间.
 * @property remainingComboTicks 当前招式可以进行连招的剩余时间.
 * @property currentBladeSpirit 当前气刃值.
 * @property currentBladeLevel 当前气刃等级, 分为无刃、白刃、黄刃、红刃.
 * @property currentAction 当前招式.
 */
data class KatanaState(
    var config: Katana,
) : EComponent<KatanaState> {
    var isArmed: Boolean = true
    var unarmedTicks: Int = 0
    var isForesightSlashHitTarget: Boolean = false
    var isForesightSlashAlreadyReward: Boolean = false
    var remainingForesightSlashTicks: Int = 0
    var remainingBladeLevelTicks: Int = -1
    var remainingComboTicks: Int = 0

    var currentBladeSpirit: Int = 0
        private set
    var currentBladeLevel: BladeLevel = BladeLevel.NONE
        private set
    var currentAction: ActionType = ActionType.IDLE
        private set

    /**
     * 展示气刃信息的 BossBar.
     * TODO 改为 HUD
     */
    val bossBar: BossBar = BossBar.bossBar(Component.empty(), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)

    companion object : EComponentType<KatanaState>() {
        /** 气刃值上限. */
        const val MAX_BLADE_SPIRIT = 100

        /** 气刃值下限. */
        const val MIN_BLADE_SPIRIT = 0
    }

    override fun type(): ComponentType<KatanaState> = KatanaState

    /**
     * 增加气刃值.
     * [value] 可以为负数, 即减少气刃值.
     */
    fun addBladeSpirit(value: Int) {
        currentBladeSpirit = (currentBladeSpirit + value).coerceIn(MIN_BLADE_SPIRIT, MAX_BLADE_SPIRIT)
    }

    /**
     * 获取太刀气刃等级伤害加成.
     */
    fun getBladeLevelDamageRate(): Double {
        return currentBladeLevel.damageRate
    }

    /**
     * 提高气刃等级.
     */
    fun upgradeBladeLevel() {
        currentBladeLevel = when (currentBladeLevel) {
            BladeLevel.NONE -> BladeLevel.WHITE
            BladeLevel.WHITE -> BladeLevel.YELLOW
            BladeLevel.YELLOW -> BladeLevel.RED
            BladeLevel.RED -> return
        }
        remainingBladeLevelTicks = currentBladeLevel.duration
    }

    /**
     * 降低气刃等级.
     */
    fun downgradeBladeLevel() {
        currentBladeLevel = when (currentBladeLevel) {
            BladeLevel.NONE -> return
            BladeLevel.WHITE -> BladeLevel.NONE
            BladeLevel.YELLOW -> BladeLevel.WHITE
            BladeLevel.RED -> BladeLevel.YELLOW
        }
        remainingBladeLevelTicks = currentBladeLevel.duration
    }

    /**
     * 切换当前招式状态.
     */
    fun changeAction(newAction: ActionType, duration: Int) {
        currentAction = newAction
        remainingComboTicks = duration
    }

    /**
     * 当前是否可以发动气刃斩1.
     */
    fun canUseSpiritBladeSlash1(): Boolean {
        return this.currentBladeSpirit >= config.spiritBladeSlashSpiritConsume1
    }

    /**
     * 当前是否可以发动气刃斩2.
     */
    fun canUseSpiritBladeSlash2(): Boolean {
        return this.currentBladeSpirit >= config.spiritBladeSlashSpiritConsume2
    }

    /**
     * 当前是否可以发动气刃斩3.
     */
    fun canUseSpiritBladeSlash3(): Boolean {
        return this.currentBladeSpirit >= config.spiritBladeSlashSpiritConsume3
    }

    /**
     * 当前是否可以发动气刃大回旋斩.
     */
    fun canUseRoundSlash(): Boolean {
        return this.currentBladeSpirit >= config.roundSlashSpiritConsume
    }

    enum class BladeLevel(
        /**
         * 该气刃等级对太刀的伤害增益.
         */
        val damageRate: Double,

        /**
         * 该气刃等级的持续时间.
         */
        val duration: Int,
    ) {
        NONE(1.0, -1),
        WHITE(1.05, 60 * 20),
        YELLOW(1.1, 45 * 20),
        RED(1.2, 30 * 20)
    }

    enum class ActionType {
        IDLE,
        HORIZONTAL_SLASH,
        SPIRIT_BLADE_SLASH_1,
        SPIRIT_BLADE_SLASH_2,
        SPIRIT_BLADE_SLASH_3,
        ROUND_SLASH,
        FORESIGHT_SLASH,
        // DRAGON_ASCEND_SLASH,
    }
}
