package cc.mewcraft.wakame.weapon

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.player.interact.PlayerClickEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.metadata.Metadata
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.runTaskTimer
import com.destroystokyo.paper.event.server.ServerTickStartEvent
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
import kotlin.jvm.optionals.getOrElse

@ConfigSerializable
data class KatanaWeapon(
    /**
     * 玩家非手持太刀时, 每秒气刃值减少量.
     */
    val notHoldSpiritConsume: Int = 20,

    /**
     * 左键攻击物品冷却.
     */
    val attackCooldown: Int = 20,

    /**
     * 左键攻击命中奖励的气刃值.
     */
    val attackSpiritReward: Int = 4,

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
    val roundSlashRadius: Double = 3.0,
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
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return null

        // 物品在冷却中, 返回 null 取消这次伤害事件
        if (nekoStack.isOnCooldown(player)) return null

        // 满气时有额外衍生效果, 返回 null 取消伤害事件
        val user = player.toUser()
        if (katanaWeaponData.bladeSpirit >= KatanaWeaponData.MAX_BLADE_SPIRIT) return null

        val attributeMap = user.attributeMap
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeMap,
            damageBundle = damageBundle(attributeMap) {
                every {
                    standard()
                    rate { katanaWeaponData.getBladeLevelDamageRate() * standard() }
                }
            }
        )
        return damageMetadata
    }

    override fun handleClick(player: Player, nekoStack: NekoStack, clickAction: PlayerClickEvent.Action, clickHand: PlayerClickEvent.Hand, event: PlayerClickEvent) {
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        if (nekoStack.isOnCooldown(player)) return

        // 玩家不是左键点击
        if (clickAction != PlayerClickEvent.Action.LEFT_CLICK) return

        val user = player.toUser()
        // 玩家非满气
        if (katanaWeaponData.bladeSpirit < KatanaWeaponData.MAX_BLADE_SPIRIT) return

        val bladeLevel = katanaWeaponData.bladeLevel
        val attributeMap = user.attributeMap
        when (bladeLevel) {
            // 发动回旋斩
            KatanaWeaponData.BladeLevel.NONE,
            KatanaWeaponData.BladeLevel.WHITE,
            KatanaWeaponData.BladeLevel.YELLOW -> {
                // 消耗所有气
                katanaWeaponData.bladeSpirit = 0
                val nearbyLivingEntities = player.location.add(.0, 0.5, .0).getNearbyLivingEntities(roundSlashRadius, .5) { it != player }
                val damageTags = DamageTags(DamageTag.MELEE, DamageTag.KATANA)
                if (nearbyLivingEntities.isNotEmpty()) {
                    // 提升气刃等级
                    katanaWeaponData.upgradeBladeLevel()
                    nearbyLivingEntities.forEach { victim ->
                        val roundSlashDamageMetadata = PlayerDamageMetadata(
                            attributes = user.attributeMap,
                            damageTags = damageTags,
                            damageBundle = damageBundle(attributeMap) {
                                every {
                                    standard()
                                    rate {
                                        katanaWeaponData.getBladeLevelDamageRate() * standard()
                                    }
                                }
                            }
                        )
                        victim.hurt(roundSlashDamageMetadata, player, true)
                    }
                    player.sendMessage("回旋斩命中！气刃等级提升！")
                } else {
                    player.sendMessage("回旋斩未命中！")
                }
            }

            // 发动登龙斩
            KatanaWeaponData.BladeLevel.RED -> {
                // 消耗所有气和一层气刃等级
                katanaWeaponData.bladeSpirit = 0
                katanaWeaponData.downgradeBladeLevel()
                // TODO 登龙效果
                player.sendMessage("登龙斩！")
            }
        }
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        // 物品在冷却中
        if (nekoStack.isOnCooldown(player)) return

        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) return

        // 是太刀普通攻击命中目标
        // 加少量气刃值
        katanaWeaponData.addBladeSpirit(attackSpiritReward)
        // 攻击冷却
        nekoStack.addCooldown(player, attackCooldown)
    }

    override fun handlePlayerDamage(player: Player, nekoStack: NekoStack, damageSource: DamageSource, event: NekoEntityDamageEvent) {
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        // 存在伤害来源实体
        val damager = damageSource.causingEntity as? LivingEntity ?: return

        val user = player.toUser()
        // 玩家处于居合斩无敌时间
        if (katanaWeaponData.laiTicks > 0) {
            event.isCancelled = true
            // 居合斩奖励只会判定一次
            if (!katanaWeaponData.isAlreadyLai) {
                katanaWeaponData.isAlreadyLai = true
                // 加大量气刃值
                katanaWeaponData.addBladeSpirit(laiSlashSpiritReward)
                // 向前位移
                val direction = player.location.direction.setY(0).normalize().multiply(laiSlashVelocityMultiply)
                player.velocity = direction
                // 对伤害来源造成伤害
                val attributeMap = user.attributeMap
                val damageMetadata = PlayerDamageMetadata(
                    attributes = attributeMap,
                    damageTags = DamageTags(DamageTag.MELEE, DamageTag.KATANA),
                    damageBundle = damageBundle(attributeMap) {
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
        }
    }

    override fun handleRelease(player: Player, nekoStack: NekoStack, event: PlayerStopUsingItemEvent) {
        val katanaWeaponData = getData(player)
        if (!katanaWeaponData.isHold) return

        // 物品在冷却中
        if (nekoStack.isOnCooldown(player)) return

        katanaWeaponData.isAlreadyLai = false
        // 气刃值不足发动正常居合斩
        val laiTicks = if (katanaWeaponData.bladeSpirit < laiSlashSpiritConsume) {
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

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        // 只允许主手使用太刀进行交互
        if (event.hand != EquipmentSlot.HAND) {
            event.setUseItemInHand(Event.Result.DENY)
        }
        wrappedEvent.actionPerformed = true
    }

    override fun handleActiveTick(player: Player, nekoStack: NekoStack, event: ServerTickStartEvent) {

    }

    override fun handleSlotChangePreviousItem(player: Player, nekoStack: NekoStack, slot: ItemSlot) {
        val katanaWeaponData = getData(player)
        katanaWeaponData.isHold = false
    }

    override fun handleSlotChangeCurrentItem(player: Player, nekoStack: NekoStack, slot: ItemSlot) {
        val katanaWeaponData = getData(player)
        val user = player.toUser()

        katanaWeaponData.isHold = true
        // 重置玩家非手持太刀的 tick 数.
        // 这意味着, 玩家非手持太刀不足1s的部分不会有惩罚.
        katanaWeaponData.outSlotTicks = 0
        if (katanaWeaponData.task == null) {
            katanaWeaponData.task = runTaskTimer(delay = 0, period = 1) {
                liveTick(user, katanaWeaponData)
            }
        }
    }

    /**
     * 方便函数.
     * 在太刀数据存在时总是 tick 的逻辑.
     */
    private fun liveTick(user: User<Player>, katanaWeaponData: KatanaWeaponData) {
        val player = user.player
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
     * 居合斩无敌时间.
     * 大于 0 意味着玩家正处于居合斩无敌判定中.
     */
    var laiTicks: Int = 0,

    /**
     * 居合斩是否已经判定成功.
     * 发动居合斩时会被设置为 false .
     */
    var isAlreadyLai: Boolean = true
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
        val duration: Int
    ) {
        NONE(1.0, -1),
        WHITE(1.05, 60 * 20),
        YELLOW(1.1, 40 * 20),
        RED(1.2, 20 * 20)
    }
}
