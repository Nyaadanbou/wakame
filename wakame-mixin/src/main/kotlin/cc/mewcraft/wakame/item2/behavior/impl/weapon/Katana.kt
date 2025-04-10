package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item2.ItemSlotChanges
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.weapon.Katana
import cc.mewcraft.wakame.item2.extension.addCooldown
import cc.mewcraft.wakame.item2.extension.isOnCooldown
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.util.adventure.SoundSource
import cc.mewcraft.wakame.util.adventure.playSound
import cc.mewcraft.wakame.util.serverTick
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import io.papermc.paper.registry.keys.SoundEventKeys
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "damage")

/**
 * 太刀的物品行为.
 */
data object Katana : Weapon {

    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        return null // 太刀没有直接点击实体造成伤害触发的逻辑, 直接返回 null 取消伤害事件
    }

    override fun handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) {
        player.sendMessage("$serverTick - 太刀左键")
        val katanaState = player.koishify().getOrNull(KatanaState) ?: return
        if (katanaState.isArmed.not()) return
        if (itemstack.isOnCooldown(player)) return

        if (!player.isSneaking) {
            // 玩家单纯左键点击, 发动横斩
            horizontalSlash(player, itemstack, katanaState)
        } else {
            // 玩家潜行左键点击, 进行复杂的连招判定
            slashCombo(player, itemstack, katanaState)
        }
    }

    override fun handleReceiveDamage(player: Player, itemstack: ItemStack, damageSource: DamageSource, event: NekoPostprocessDamageEvent) {
        val katanaState = player.koishify().getOrNull(KatanaState) ?: return
        if (katanaState.isArmed.not()) return

        laiSlashCheck(player, katanaState, event)
    }

    override fun handleRelease(player: Player, itemstack: ItemStack, event: PlayerStopUsingItemEvent) {
        val katanaState = player.koishify().getOrNull(KatanaState) ?: return
        if (katanaState.isArmed.not()) return
        if (itemstack.isOnCooldown(player)) return

        laiSlash(player, itemstack, katanaState)
    }

    override fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        if (event.hand != EquipmentSlot.HAND) {
            event.setUseItemInHand(Event.Result.DENY)
        }
        wrappedEvent.actionPerformed = true
    }

    /**
     * 太刀横斩.
     */
    private fun horizontalSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate { // override
                        katanaState.getBladeLevelDamageRate() * standard()
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
            katanaState.addBladeSpirit(katanaConfig.horizontalSlashSpiritReward)
        }
    }

    /**
     * 太刀居合斩.
     */
    private fun laiSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        katanaState.isAlreadyLai = false
        val katanaConfig = katanaState.config
        val laiTicks = if (katanaState.bladeSpirit < katanaConfig.laiSlashSpiritConsume) {
            // 气刃值不足发动正常居合斩 - 扣除全部气刃值
            katanaState.bladeSpirit = 0
            katanaConfig.weakLaiSlashTicks
        } else {
            // 扣除所需气刃值
            katanaState.addBladeSpirit(-katanaConfig.laiSlashSpiritConsume)
            katanaConfig.laiSlashTicks
        }
        katanaState.laiTicks = laiTicks
        // 攻击冷却
        itemstack.addCooldown(player, katanaConfig.laiSlashCooldown)
    }

    /**
     * 太刀居合斩判定.
     */
    private fun laiSlashCheck(player: Player, katanaState: KatanaState, event: NekoPostprocessDamageEvent) {
        val katanaConfig = katanaState.config
        // 不存在伤害来源实体 - 不处理
        val damager = event.damageSource.causingEntity as? LivingEntity ?: return
        // 玩家不处于居合斩无敌时间 - 不处理
        if (katanaState.laiTicks <= 0) return
        // 处于无敌时间都取消伤害, 即使居合斩已经判定成功过了.
        event.isCancelled = true
        // 居合斩奖励只判定一次
        if (katanaState.isAlreadyLai) return
        katanaState.isAlreadyLai = true
        // 加气刃值
        katanaState.addBladeSpirit(katanaConfig.laiSlashSpiritReward)
        // 向前位移
        val force = player.location.direction.setY(0).normalize().multiply(katanaConfig.laiSlashVelocityMultiply)
        player.velocity = force
        // 对伤害来源造成伤害
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate {
                        katanaState.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )
        damager.hurt(damageMetadata, player, true)
        // TODO 音效和特效: 叮~
        player.playSound(player) {
            type(SoundEventKeys.ENTITY_EXPERIENCE_ORB_PICKUP)
            source(SoundSource.PLAYER)
        }
    }

    /**
     * 方便函数.
     * 用于太刀气刃斩连段123.
     */
    private fun spiritBladeSlashBase(player: Player, katanaState: KatanaState, angel: Float) {
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate { // override
                        katanaState.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )
        val hitEntities = getHitEntities(player, 5.0, 1.7f, 0.05f, 1.4f, angel)
        // 造成伤害
        hitEntities.forEach { entity -> entity.hurt(damageMetadata, player, true) }
    }

    /**
     * 太刀气刃斩连段1.
     * 左气刃斩.
     */
    private fun spiritBladeSlash1(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-katanaConfig.spiritBladeSlashSpiritConsume1)
        spiritBladeSlashBase(player, katanaState, 35f)
        // 连招状态
        katanaState.spiritBladeSlashState = KatanaState.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_1
        katanaState.spiritBladeSlashComboTicks = katanaConfig.spiritBladeSlashAllowComboTicks
        // 冷却
        itemstack.addCooldown(player, katanaConfig.spiritBladeSlashCooldown1)
    }

    /**
     * 太刀气刃斩连段2.
     * 右气刃斩.
     */
    private fun spiritBladeSlash2(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-katanaConfig.spiritBladeSlashSpiritConsume2)
        spiritBladeSlashBase(player, katanaState, -35f)
        // 连招状态
        katanaState.spiritBladeSlashState = KatanaState.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_2
        katanaState.spiritBladeSlashComboTicks = katanaConfig.spiritBladeSlashAllowComboTicks
        // 冷却
        itemstack.addCooldown(player, katanaConfig.spiritBladeSlashCooldown2)
    }

    /**
     * 太刀气刃斩连段3.
     * 左右气刃二连斩.
     */
    private fun spiritBladeSlash3(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-katanaConfig.spiritBladeSlashSpiritConsume3)
        spiritBladeSlashBase(player, katanaState, 35f)
        spiritBladeSlashBase(player, katanaState, -35f)
        // 连招状态
        katanaState.spiritBladeSlashState = KatanaState.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_3
        katanaState.spiritBladeSlashComboTicks = katanaConfig.spiritBladeSlashAllowComboTicks
        // 冷却
        itemstack.addCooldown(player, katanaConfig.spiritBladeSlashCooldown3)
    }

    /**
     * 太刀气刃大回旋斩.
     */
    private fun roundSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        // 消耗气
        katanaState.addBladeSpirit(-katanaConfig.roundSlashSpiritConsume)

        // 造成伤害
        val centerLocation = player.location.add(.0, player.boundingBox.height / 2, .0)
        val scale = player.boundingBoxScale
        val hitEntities = centerLocation.getNearbyLivingEntities(katanaConfig.roundSlashRadius * scale, 1.1 * scale) { it != player }
        val attributeContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(
            attributes = attributeContainer,
            damageBundle = damageBundle(attributeContainer) {
                every {
                    standard()
                    rate {
                        katanaState.getBladeLevelDamageRate() * standard()
                    }
                }
            }
        )
        if (hitEntities.isNotEmpty()) {
            hitEntities.forEach { entity ->
                entity.hurt(damageMetadata, player, true)
            }
            // 提升气刃等级
            katanaState.upgradeBladeLevel()
            if (LOGGING) player.sendMessage("回旋斩命中! 气刃等级提升!")
        } else {
            if (LOGGING) player.sendMessage("回旋斩未命中!")
        }

        // 更新连招状态
        katanaState.spiritBladeSlashState = KatanaState.SpiritBladeSlashState.NONE
        katanaState.spiritBladeSlashComboTicks = 0

        // 更新冷却
        itemstack.addCooldown(player, katanaConfig.roundSlashCooldown)
    }

    /**
     * 太刀连招.
     */
    private fun slashCombo(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        val comboTicks = katanaState.spiritBladeSlashComboTicks
        val state = katanaState.spiritBladeSlashState
        when (state) {
            KatanaState.SpiritBladeSlashState.NONE -> {
                // 气刃值足够发动气刃斩1
                if (katanaState.bladeSpirit >= katanaConfig.spiritBladeSlashSpiritConsume1) {
                    spiritBladeSlash1(player, itemstack, katanaState)
                } else {
                    horizontalSlash(player, itemstack, katanaState)
                }
            }

            KatanaState.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_1 -> {
                // 气刃值足够发动气刃斩2, 且处于连招允许时间内
                if (katanaState.bladeSpirit >= katanaConfig.spiritBladeSlashSpiritConsume2 && comboTicks > 0) {
                    spiritBladeSlash2(player, itemstack, katanaState)
                } else {
                    horizontalSlash(player, itemstack, katanaState)
                }
            }

            KatanaState.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_2 -> {
                // 气刃值足够发动气刃斩3, 且处于连招允许时间内
                if (katanaState.bladeSpirit >= katanaConfig.spiritBladeSlashSpiritConsume3 && comboTicks > 0) {
                    spiritBladeSlash3(player, itemstack, katanaState)
                } else {
                    horizontalSlash(player, itemstack, katanaState)
                }
            }

            KatanaState.SpiritBladeSlashState.SPIRIT_BLADE_SLASH_3 -> {
                val bladeLevel = katanaState.bladeLevel
                when (bladeLevel) {

                    // 非红刃, 尝试发动回旋斩
                    KatanaState.BladeLevel.NONE,
                    KatanaState.BladeLevel.WHITE,
                    KatanaState.BladeLevel.YELLOW,
                        -> {
                        // 气刃值足够发动回旋斩, 且处于连招允许时间内
                        if (katanaState.bladeSpirit >= katanaConfig.roundSlashSpiritConsume && comboTicks > 0) {
                            roundSlash(player, itemstack, katanaState)
                        } else {
                            horizontalSlash(player, itemstack, katanaState)
                        }
                    }

                    // 红刃, 尝试发动登龙斩
                    KatanaState.BladeLevel.RED -> {
                        // 气刃值足够发动登龙斩, 且处于连招允许时间内
                        if (katanaState.bladeSpirit >= katanaConfig.dragonAscendSlashSpiritConsume && comboTicks > 0) {
                            dragonAscendSlash(player, itemstack, katanaState)
                        } else {
                            horizontalSlash(player, itemstack, katanaState)
                        }
                    }
                }
            }
        }
    }

    /**
     * 太刀气刃登龙斩.
     */
    private fun dragonAscendSlash(player: Player, itemstack: ItemStack, katanaState: KatanaState) {
        val katanaConfig = katanaState.config
        // 消耗气和一层气刃等级
        katanaState.addBladeSpirit(-katanaConfig.dragonAscendSlashSpiritConsume)
        katanaState.downgradeBladeLevel()

        // TODO 登龙效果

        itemstack.addCooldown(player, katanaConfig.dragonAscendSlashCooldown)

        if (LOGGING) player.sendMessage("登龙斩!")
    }
}

/**
 * 切换手持太刀时的逻辑.
 */
data object SwitchKatana : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        val katanaState = entity.getOrNull(KatanaState)
        slotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null &&
                ItemSlotChanges.testSlot(slot, prev)
            ) {
                val katanaItem = prev.getProperty(ItemPropertyTypes.KATANA)
                if (katanaItem != null && katanaState != null) {
                    katanaState.isArmed = false
                }
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val katanaConfig = curr.getProperty(ItemPropertyTypes.KATANA)
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
/**
 * tick 太刀状态的逻辑.
 */
data object TickKatana : IteratingSystem(
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
                if (katanaState.bladeSpirit <= 0 && katanaState.bladeLevel == KatanaState.BladeLevel.NONE) {
                    katanaState.bossBar.removeViewer(player)
                    entity.configure { it -= KatanaState }
                    return
                }
            }
        }

        // 气刃等级持续时间自减和降级
        if (katanaState.bladeLevelTicks > 0) {
            katanaState.bladeLevelTicks -= 1
            if (katanaState.bladeLevelTicks == 0) {
                katanaState.downgradeBladeLevel()
            }
        }

        // 气刃斩连段时间自减
        if (katanaState.spiritBladeSlashComboTicks > 0) {
            katanaState.spiritBladeSlashComboTicks -= 1
            if (katanaState.spiritBladeSlashComboTicks <= 0) {
                katanaState.spiritBladeSlashState = KatanaState.SpiritBladeSlashState.NONE
            }
        }

        // 居合无敌时间自减
        if (katanaState.laiTicks > 0) {
            katanaState.laiTicks -= 1
        }

        // 更新 BossBar
        val bossBar = katanaState.bossBar
        val progress = katanaState.bladeSpirit.toFloat() / KatanaState.MAX_BLADE_SPIRIT
        val text = Component.text("气刃值 ${katanaState.bladeSpirit} / ${KatanaState.MAX_BLADE_SPIRIT}")
        val color = when (katanaState.bladeLevel) {
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
 * @property unarmedTicks 玩家非手持太刀累计 tick 数.
 * @property bladeSpirit 气刃值.
 * @property bladeLevel 气刃等级, 分为无刃、白刃、黄刃、红刃.
 * @property bossBar 展示气刃信息的 BossBar
 * @property bladeLevelTicks 当前气刃等级的持续时间.
 * @property spiritBladeSlashComboTicks 玩家可以继续气刃斩连段的剩余时间.
 * @property spiritBladeSlashState 玩家气刃斩连段的状态.
 * @property laiTicks 居合斩无敌时间. 大于 0 意味着玩家正处于居合斩无敌判定中.
 * @property isAlreadyLai 居合斩是否已经判定成功. 发动居合斩时会被设置为 `false`.
 */
data class KatanaState(
    var config: Katana,
    var isArmed: Boolean = true,
    var unarmedTicks: Int = 0,
    var bladeSpirit: Int = 0,
    var bladeLevel: BladeLevel = BladeLevel.NONE,
    val bossBar: BossBar = BossBar.bossBar(Component.empty(), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS),
    var bladeLevelTicks: Int = -1,
    var spiritBladeSlashComboTicks: Int = 0,
    var spiritBladeSlashState: SpiritBladeSlashState = SpiritBladeSlashState.NONE,
    var laiTicks: Int = 0,
    var isAlreadyLai: Boolean = true,
) : EComponent<KatanaState> {

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
