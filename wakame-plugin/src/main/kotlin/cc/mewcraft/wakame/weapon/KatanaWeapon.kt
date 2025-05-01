package cc.mewcraft.wakame.weapon

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.util.metadata.Metadata
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.runTaskTimer
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitTask
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.xenondevs.commons.provider.orElse
import kotlin.jvm.optionals.getOrElse

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

@ConfigSerializable
@Deprecated("过时")
data class KatanaWeapon(
    /**
     * 玩家非手持太刀时, 每秒气刃值减少量.
     */
    val notHoldSpiritConsume: Int = 5,

    /**
     * 横斩后物品冷却.
     */
    val horizontalSlashCooldown: Int = 14,

    /**
     * 横斩命中奖励的气刃值.
     */
    val horizontalSlashSpiritReward: Int = 4,

    /**
     * 发动气刃斩1所需的气刃值.
     */
    val spiritBladeSlashSpiritConsume1: Int = 15,

    /**
     * 发动气刃斩2所需的气刃值.
     */
    val spiritBladeSlashSpiritConsume2: Int = 15,

    /**
     * 发动气刃斩3所需的气刃值.
     */
    val spiritBladeSlashSpiritConsume3: Int = 20,

    /**
     * 气刃斩1后物品冷却.
     */
    val spiritBladeSlashCooldown1: Int = 10,

    /**
     * 气刃斩2后物品冷却.
     */
    val spiritBladeSlashCooldown2: Int = 10,

    /**
     * 气刃斩3后物品冷却.
     */
    val spiritBladeSlashCooldown3: Int = 12,

    /**
     * 气刃斩连段允许的时间.
     * 在此时间内才能成功连段.
     */
    val spiritBladeSlashAllowComboTicks: Int = 40,

    /**
     * 发动正常居合斩所需的气刃值.
     */
    val laiSlashSpiritConsume: Int = 10,

    /**
     * 居合斩后物品冷却.
     */
    val laiSlashCooldown: Int = 20,

    /**
     * 发动正常居合斩给予的无敌 tick 数.
     */
    val laiSlashTicks: Int = 15,

    /**
     * 气刃值不足时发动居合斩给予的无敌 tick 数.
     */
    val weakLaiSlashTicks: Int = 5,

    /**
     * 居合斩命中奖励的气刃值.
     */
    val laiSlashSpiritReward: Int = 50,

    /**
     * 居合斩位移速度倍率.
     */
    val laiSlashVelocityMultiply: Double = 3.0,

    /**
     * 回旋斩半径.
     */
    val roundSlashRadius: Double = 3.5,

    /**
     * 发动回旋斩所需的气刃值.
     */
    val roundSlashSpiritConsume: Int = 20,

    /**
     * 回旋斩后物品冷却.
     */
    val roundSlashCooldown: Int = 20,

    /**
     * 发动登龙斩所需的气刃值.
     */
    val dragonAscendSlashSpiritConsume: Int = 25,

    /**
     * 登龙斩后物品冷却.
     */
    val dragonAscendSlashCooldown: Int = 40,
) : WeaponType {
    companion object {
        private val KATANA_DATA_KEY: MetadataKey<KatanaWeaponData> = MetadataKey.create("katana_data", KatanaWeaponData::class.java)

        fun getData(player: Player): KatanaWeaponData {
            val playerMap = Metadata.provideForPlayer(player)
            return playerMap[KATANA_DATA_KEY].getOrElse {
                val default = KatanaWeaponData()
                playerMap.put(KATANA_DATA_KEY, default)
                default
            }
        }

        fun removeData(player: Player) {
            val playerMap = Metadata.provideForPlayer(player)
            playerMap.remove(KATANA_DATA_KEY)
        }
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        // 太刀没有直接点击实体造成伤害触发的逻辑
        // 直接返回 null 取消伤害事件
        return null
    }

    override fun handleLeftClick(player: Player, nekoStack: NekoStack, event: PlayerItemLeftClickEvent) {
        player.sendMessage("太刀左键")
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        if (nekoStack.isOnCooldown(player)) return

        if (!player.isSneaking) {
            // 玩家单纯左键点击
            // 发动横斩
            horizontalSlash(player, nekoStack, katanaWeaponData)
        } else {
            // 玩家潜行左键点击
            // 进行复杂的连招判定
            slashCombo(player, nekoStack, katanaWeaponData)
        }
    }

    override fun handlePlayerDamage(player: Player, nekoStack: NekoStack, damageSource: DamageSource, event: PostprocessDamageEvent) {
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        laiSlashCheck(player, katanaWeaponData, event)
    }

    override fun handleRelease(player: Player, nekoStack: NekoStack, event: PlayerStopUsingItemEvent) {
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        // 物品在冷却中
        if (nekoStack.isOnCooldown(player)) return

        laiSlash(player, nekoStack, katanaWeaponData)
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        // 只允许主手使用太刀进行交互
        if (event.hand != EquipmentSlot.HAND) {
            event.setUseItemInHand(Event.Result.DENY)
        }
        wrappedEvent.actionPerformed = true
    }

    override fun handleSlotChangePreviousItem(player: Player, nekoStack: NekoStack, slot: ItemSlot) {
        val katanaWeaponData = getData(player)
        katanaWeaponData.isHold = false
    }

    override fun handleSlotChangeCurrentItem(player: Player, nekoStack: NekoStack, slot: ItemSlot) {
        val katanaWeaponData = getData(player)

        katanaWeaponData.isHold = true
        // 重置玩家非手持太刀的 tick 数.
        // 这意味着, 玩家非手持太刀不足1s的部分不会有惩罚.
        katanaWeaponData.outSlotTicks = 0
        if (katanaWeaponData.task == null) {
            katanaWeaponData.task = runTaskTimer(delay = 0, period = 1) {
                liveTick(player, katanaWeaponData)
            }
        }
    }

    /**
     * 太刀横斩.
     */
    private fun horizontalSlash(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.KATANA),
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate {
                        katanaWeaponData.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )

        val hitEntities = getHitEntities(player, 5.0, 1.2f, 0.05f, 1.1f)
        if (hitEntities.isNotEmpty()) {
            // 造成伤害
            hitEntities.forEach { entity ->
                entity.hurt(damageMetadata, player, true)
            }
            // 增加气刃值
            katanaWeaponData.addBladeSpirit(horizontalSlashSpiritReward)
        }
        // 攻击冷却
        nekoStack.addCooldown(player, horizontalSlashCooldown)
    }

    /**
     * 太刀居合斩.
     */
    private fun laiSlash(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        katanaWeaponData.isAlreadyLai = false
        val laiTicks = if (katanaWeaponData.bladeSpirit < laiSlashSpiritConsume) {
            // 气刃值不足发动正常居合斩
            // 扣除全部气刃值
            katanaWeaponData.bladeSpirit = 0
            weakLaiSlashTicks
        } else {
            // 扣除所需气刃值
            katanaWeaponData.addBladeSpirit(-laiSlashSpiritConsume)
            laiSlashTicks
        }

        katanaWeaponData.laiTicks = laiTicks
        // 攻击冷却
        nekoStack.addCooldown(player, laiSlashCooldown)
    }

    /**
     * 太刀居合斩判定.
     */
    private fun laiSlashCheck(player: Player, katanaWeaponData: KatanaWeaponData, event: PostprocessDamageEvent) {
        // 不存在伤害来源实体, 不处理
        val damager = event.damageSource.causingEntity as? LivingEntity ?: return

        // 玩家不处于居合斩无敌时间, 不处理
        if (katanaWeaponData.laiTicks <= 0) return

        // 处于无敌时间都取消伤害.
        // 即使居合斩已经判定成功过了.
        event.isCancelled = true

        // 居合斩奖励只判定一次
        if (katanaWeaponData.isAlreadyLai) return

        katanaWeaponData.isAlreadyLai = true
        // 加气刃值
        katanaWeaponData.addBladeSpirit(laiSlashSpiritReward)
        // 向前位移
        val direction = player.location.direction.setY(0).normalize().multiply(laiSlashVelocityMultiply)
        player.velocity = direction
        // 对伤害来源造成伤害
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.KATANA),
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate {
                        katanaWeaponData.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )
        damager.hurt(damageMetadata, player, true)

        // TODO 音效和特效：叮~
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F)
    }

    /**
     * 方便函数.
     * 用于太刀气刃斩连段123.
     */
    private fun spiritBladeSlashBase(player: Player, katanaWeaponData: KatanaWeaponData, angel: Float) {
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.KATANA),
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate {
                        katanaWeaponData.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )

        val hitEntities = getHitEntities(player, 5.0, 1.7f, 0.05f, 1.4f, angel)
        // 造成伤害
        hitEntities.forEach { entity ->
            entity.hurt(damageMetadata, player, true)
        }
    }

    /**
     * 太刀气刃斩连段1.
     * 左气刃斩.
     */
    private fun spiritBladeSlash1(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        // 消耗气
        katanaWeaponData.addBladeSpirit(-spiritBladeSlashSpiritConsume1)
        spiritBladeSlashBase(player, katanaWeaponData, 35f)
        // 连招状态
        katanaWeaponData.spiritBladeSlashState = KatanaWeaponData.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_1
        katanaWeaponData.spiritBladeSlashComboTicks = spiritBladeSlashAllowComboTicks
        // 冷却
        nekoStack.addCooldown(player, spiritBladeSlashCooldown1)
    }

    /**
     * 太刀气刃斩连段2.
     * 右气刃斩.
     */
    private fun spiritBladeSlash2(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        // 消耗气
        katanaWeaponData.addBladeSpirit(-spiritBladeSlashSpiritConsume2)
        spiritBladeSlashBase(player, katanaWeaponData, -35f)
        // 连招状态
        katanaWeaponData.spiritBladeSlashState = KatanaWeaponData.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_2
        katanaWeaponData.spiritBladeSlashComboTicks = spiritBladeSlashAllowComboTicks
        // 冷却
        nekoStack.addCooldown(player, spiritBladeSlashCooldown2)
    }

    /**
     * 太刀气刃斩连段3.
     * 左右气刃二连斩.
     */
    private fun spiritBladeSlash3(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        // 消耗气
        katanaWeaponData.addBladeSpirit(-spiritBladeSlashSpiritConsume3)
        spiritBladeSlashBase(player, katanaWeaponData, 35f)
        spiritBladeSlashBase(player, katanaWeaponData, -35f)
        // 连招状态
        katanaWeaponData.spiritBladeSlashState = KatanaWeaponData.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_3
        katanaWeaponData.spiritBladeSlashComboTicks = spiritBladeSlashAllowComboTicks
        // 冷却
        nekoStack.addCooldown(player, spiritBladeSlashCooldown3)
    }

    /**
     * 太刀气刃大回旋斩.
     */
    private fun roundSlash(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        // 消耗气
        katanaWeaponData.addBladeSpirit(-roundSlashSpiritConsume)

        val centerLocation = player.location.add(.0, player.boundingBox.height / 2, .0)
        val scale = player.boundingBoxScale
        val hitEntities = centerLocation.getNearbyLivingEntities(roundSlashRadius * scale, 1.1 * scale) {
            it != player
        }

        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.KATANA),
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate {
                        katanaWeaponData.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )

        if (hitEntities.isNotEmpty()) {
            hitEntities.forEach { entity ->
                entity.hurt(damageMetadata, player, true)
            }
            // 提升气刃等级
            katanaWeaponData.upgradeBladeLevel()

            if (LOGGING) {
                player.sendMessage("回旋斩命中！气刃等级提升！")
            }
        } else {
            if (LOGGING) {
                player.sendMessage("回旋斩未命中！")
            }
        }
        // 连招状态
        katanaWeaponData.spiritBladeSlashState = KatanaWeaponData.SpiritBladeSlashState.NONE
        katanaWeaponData.spiritBladeSlashComboTicks = 0
        // 冷却
        nekoStack.addCooldown(player, roundSlashCooldown)
    }

    /**
     * 太刀连招.
     */
    private fun slashCombo(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        val comboTicks = katanaWeaponData.spiritBladeSlashComboTicks
        val state = katanaWeaponData.spiritBladeSlashState
        when (state) {
            KatanaWeaponData.SpiritBladeSlashState.NONE -> {
                // 气刃值足够发动气刃斩1
                if (katanaWeaponData.bladeSpirit >= spiritBladeSlashSpiritConsume1) {
                    spiritBladeSlash1(player, nekoStack, katanaWeaponData)
                } else {
                    horizontalSlash(player, nekoStack, katanaWeaponData)
                }
            }

            KatanaWeaponData.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_1 -> {
                // 气刃值足够发动气刃斩2
                // 且处于连招允许时间内
                if (katanaWeaponData.bladeSpirit >= spiritBladeSlashSpiritConsume2 && comboTicks > 0) {
                    spiritBladeSlash2(player, nekoStack, katanaWeaponData)
                } else {
                    horizontalSlash(player, nekoStack, katanaWeaponData)
                }
            }

            KatanaWeaponData.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_2 -> {
                // 气刃值足够发动气刃斩3
                // 且处于连招允许时间内
                if (katanaWeaponData.bladeSpirit >= spiritBladeSlashSpiritConsume3 && comboTicks > 0) {
                    spiritBladeSlash3(player, nekoStack, katanaWeaponData)
                } else {
                    horizontalSlash(player, nekoStack, katanaWeaponData)
                }
            }

            KatanaWeaponData.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_3 -> {
                val bladeLevel = katanaWeaponData.bladeLevel
                when (bladeLevel) {
                    // 非红刃, 尝试发动回旋斩
                    KatanaWeaponData.BladeLevel.NONE,
                    KatanaWeaponData.BladeLevel.WHITE,
                    KatanaWeaponData.BladeLevel.YELLOW,
                        -> {
                        // 气刃值足够发动回旋斩
                        // 且处于连招允许时间内
                        if (katanaWeaponData.bladeSpirit >= roundSlashSpiritConsume && comboTicks > 0) {
                            roundSlash(player, nekoStack, katanaWeaponData)
                        } else {
                            horizontalSlash(player, nekoStack, katanaWeaponData)
                        }
                    }

                    // 红刃, 尝试发动登龙斩
                    KatanaWeaponData.BladeLevel.RED -> {
                        // 气刃值足够发动登龙斩
                        // 且处于连招允许时间内
                        if (katanaWeaponData.bladeSpirit >= dragonAscendSlashSpiritConsume && comboTicks > 0) {
                            dragonAscendSlash(player, nekoStack, katanaWeaponData)
                        } else {
                            horizontalSlash(player, nekoStack, katanaWeaponData)
                        }
                    }
                }
            }
        }
    }

    /**
     * 太刀气刃登龙斩.
     */
    private fun dragonAscendSlash(player: Player, nekoStack: NekoStack, katanaWeaponData: KatanaWeaponData) {
        // 消耗气和一层气刃等级
        katanaWeaponData.addBladeSpirit(-dragonAscendSlashSpiritConsume)
        katanaWeaponData.downgradeBladeLevel()

        // TODO 登龙效果

        nekoStack.addCooldown(player, dragonAscendSlashCooldown)

        if (LOGGING) {
            player.sendMessage("登龙斩！")
        }
    }

    /**
     * 在太刀数据存在时总是 tick 的逻辑.
     */
    private fun liveTick(player: Player, katanaWeaponData: KatanaWeaponData) {
        if (!katanaWeaponData.isHold) {
            // 手持非太刀时气刃值自动降低
            katanaWeaponData.outSlotTicks += 1
            if (katanaWeaponData.outSlotTicks >= 20) {
                katanaWeaponData.outSlotTicks = 0
                katanaWeaponData.addBladeSpirit(-notHoldSpiritConsume)
                // 如果玩家的气刃值为0, 气刃等级为无刃
                // 则不再记录数据, 也不再tick
                if (katanaWeaponData.bladeSpirit <= 0 && katanaWeaponData.bladeLevel == KatanaWeaponData.BladeLevel.NONE) {
                    katanaWeaponData.task?.cancel()
                    katanaWeaponData.bossBar.removeViewer(player)
                    removeData(player)
                    return
                }
            }
        }

        // 气刃等级持续时间自减和降级
        if (katanaWeaponData.bladeLevelTicks > 0) {
            katanaWeaponData.bladeLevelTicks -= 1
            if (katanaWeaponData.bladeLevelTicks == 0) {
                katanaWeaponData.downgradeBladeLevel()
            }
        }

        // 气刃斩连段时间自减
        if (katanaWeaponData.spiritBladeSlashComboTicks > 0) {
            katanaWeaponData.spiritBladeSlashComboTicks -= 1
            if (katanaWeaponData.spiritBladeSlashComboTicks <= 0) {
                katanaWeaponData.spiritBladeSlashState = KatanaWeaponData.SpiritBladeSlashState.NONE
            }
        }

        // 居合无敌时间自减
        if (katanaWeaponData.laiTicks > 0) {
            katanaWeaponData.laiTicks -= 1
        }

        // 更新bossbar
        val bossBar = katanaWeaponData.bossBar
        val progress = katanaWeaponData.bladeSpirit.toFloat() / KatanaWeaponData.MAX_BLADE_SPIRIT.toFloat()
        val text = text { content("气刃值 ${katanaWeaponData.bladeSpirit} / ${KatanaWeaponData.MAX_BLADE_SPIRIT}") }
        val color = when (katanaWeaponData.bladeLevel) {
            KatanaWeaponData.BladeLevel.NONE -> BossBar.Color.BLUE
            KatanaWeaponData.BladeLevel.WHITE -> BossBar.Color.WHITE
            KatanaWeaponData.BladeLevel.YELLOW -> BossBar.Color.YELLOW
            KatanaWeaponData.BladeLevel.RED -> BossBar.Color.RED
        }
        bossBar.name(text)
        bossBar.progress(progress)
        bossBar.color(color)
        bossBar.addViewer(player)
    }

}

/**
 * 用于记录玩家使用太刀时的相关数据.
 */
data class KatanaWeaponData(
    /**
     * 玩家是否手持太刀太刀.
     */
    var isHold: Boolean = true,

    /**
     * 正在 tick 的任务.
     */
    var task: BukkitTask? = null,

    /**
     * 玩家非手持太刀累计 tick 数.
     */
    var outSlotTicks: Int = 0,

    /**
     * 气刃值.
     */
    var bladeSpirit: Int = 0,

    /**
     * 气刃等级, 分为无刃、白刃、黄刃、红刃.
     */
    var bladeLevel: BladeLevel = BladeLevel.NONE,

    /**
     * 展示气刃信息的 bossbar
     */
    val bossBar: BossBar = BossBar.bossBar(Component.empty(), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS),

    /**
     * 当前气刃等级的持续时间.
     */
    var bladeLevelTicks: Int = -1,

    /**
     * 玩家可以继续气刃斩连段的剩余时间.
     */
    var spiritBladeSlashComboTicks: Int = 0,

    /**
     * 玩家气刃斩连段的状态.
     */
    var spiritBladeSlashState: SpiritBladeSlashState = SpiritBladeSlashState.NONE,

    /**
     * 居合斩无敌时间.
     * 大于 0 意味着玩家正处于居合斩无敌判定中.
     */
    var laiTicks: Int = 0,

    /**
     * 居合斩是否已经判定成功.
     * 发动居合斩时会被设置为 false .
     */
    var isAlreadyLai: Boolean = true,
) {
    companion object {
        /**
         * 气刃值上限.
         */
        const val MAX_BLADE_SPIRIT = 100

        /**
         * 气刃值下限.
         */
        const val MIN_BLADE_SPIRIT = 0
    }

    /**
     * 增加气刃值.
     * [value] 可以为负数, 即减少气刃值.
     */
    fun addBladeSpirit(value: Int) {
        bladeSpirit = (bladeSpirit + value).coerceIn(MIN_BLADE_SPIRIT, MAX_BLADE_SPIRIT)
    }

    /**
     * 获取太刀气刃等级伤害加成.
     */
    fun getBladeLevelDamageRate(): Double {
        return bladeLevel.damageRate
    }

    /**
     * 提高气刃等级.
     */
    fun upgradeBladeLevel() {
        bladeLevel = when (bladeLevel) {
            BladeLevel.NONE -> BladeLevel.WHITE
            BladeLevel.WHITE -> BladeLevel.YELLOW
            BladeLevel.YELLOW -> BladeLevel.RED
            BladeLevel.RED -> return
        }
        bladeLevelTicks = bladeLevel.duration
    }

    /**
     * 降低气刃等级.
     */
    fun downgradeBladeLevel() {
        bladeLevel = when (bladeLevel) {
            BladeLevel.NONE -> return
            BladeLevel.WHITE -> BladeLevel.NONE
            BladeLevel.YELLOW -> BladeLevel.WHITE
            BladeLevel.RED -> BladeLevel.YELLOW
        }
        bladeLevelTicks = bladeLevel.duration
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
        YELLOW(1.1, 40 * 20),
        RED(1.2, 20 * 20)
    }

    /**
     * 太刀的气刃斩状态.
     */
    enum class SpiritBladeSlashState {
        NONE,
        SPIRIT_BLADE_SLASH_1,
        SPIRIT_BLADE_SLASH_2,
        SPIRIT_BLADE_SLASH_3,
    }
}
