package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.LinearComponents
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.*
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import xyz.xenondevs.commons.provider.orElse

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

/**
 * 伤害系统的监听器, 也是游戏内伤害计算逻辑的代码入口.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 由于 MythicMobs 的各种问题, priority 必须设置为 MONITOR.
    // 整个项目中, Bukkit伤害事件应只在此处被监听
    /**
     * 伤害计算流程(严格顺序, 顺序变化会导致计算结果变化):
     * - 基于 Koish 属性系统, 考虑伤害发起者攻击力和伤害承受者防御力等属性, 计算各元素伤害作为 [BASE]
     * - 计算无懈可击时间对伤害的影响
     * - 计算格挡机制对伤害的影响
     * - 计算抗性提升药水效果对伤害的影响
     * - 计算伤害吸收对伤害的影响
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDamageEvent) {
        // 只处理伤害承受者是生物的情况
        // TODO 考虑非生物
        val damagee = event.entity as? LivingEntity ?: return
        // 提取 Bukkit 伤害事件中的有用信息生成上下文
        val damageContext = DamageContext(event)

        // 计算攻击阶段的伤害信息
        // 考虑伤害发起者对伤害值的各种影响
        // 伤害信息为空时取消伤害事件
        val damageMetadata = DamageManager.calculateAttackPhaseMetadata(damageContext) ?: run {
            event.isCancelled = true
            return
        }

        // 计算防御阶段的伤害信息
        // 考虑伤害承受者对伤害值的各种影响
        // 伤害信息为空时取消伤害事件
        val defenseMetadata = DamageManager.calculateDefensePhaseMetadata(damageContext) ?: run {
            event.isCancelled = true
            return
        }

        // 依次计算 Koshi 伤害系统用到的原版伤害修饰器
        // 由于原版中某些伤害附带效果只能通过相应修饰器实现, 如增加相应统计信息/扣除黄心等
        // 故不能将伤害计算全赶到 BASE 中, 必须考虑某些原版伤害修饰器
        // 若后续伤害事件未取消, 将修改用到的修饰器, 置零未用到的修饰器
        val baseDamage = DamageManager.calculateBaseModifierDamage(damageMetadata, defenseMetadata)
        var modifiedDamage = baseDamage
        // [INVULNERABILITY_REDUCTION] - 无懈可击时间伤害减免
        val invulnerabilityDamageModifier = DamageManager.calculateInvulnerabilityModifierDamage(modifiedDamage, damageMetadata, defenseMetadata)
        invulnerabilityDamageModifier?.let { modifiedDamage += it }
        // [BLOCKING] - 格挡伤害减免
        val blockingDamageModifier = DamageManager.calculateBlockingModifierDamage(modifiedDamage, damageMetadata, defenseMetadata)
        blockingDamageModifier?.let { modifiedDamage += it }
        // [RESISTANCE] - 抗性提升药水效果伤害减免
        val resistanceDamageModifier = DamageManager.calculateResistanceModifierDamage(modifiedDamage, damageMetadata, defenseMetadata)
        resistanceDamageModifier?.let { modifiedDamage += it }
        // [ABSORPTION] - 伤害吸收
        val absorptionDamageModifier = DamageManager.calculateAbsorptionModifierDamage(modifiedDamage, damageMetadata, defenseMetadata)
        // absorptionDamageModifier?.let { modifiedDamage += it } // 最后一个就不用加了

        val postprocessEvent = PostprocessDamageEvent(damageMetadata, defenseMetadata, finalDamageMap, event)
        if (!postprocessEvent.callEvent()) {
            // Koish 伤害事件被取消, 则直接返回
            // Koish 伤害事件被取消时, 其内部的 Bukkit 伤害事件必然是取消的状态
            return
        }

        // 修改 BASE 伤害
        event.modifyDamageModifier(BASE, baseDamage)
        // 置零 Koish 伤害系统不考虑的原版伤害修饰器
        event.removeUnusedDamageModifiers()
        // 修改 Koish 伤害系统考虑的原版伤害修饰器
        event.modifyUsedDamageModifiers(invulnerabilityDamageModifier, blockingDamageModifier, resistanceDamageModifier, absorptionDamageModifier)

        // 记录日志
        if (LOGGING) event.logging(damagee)
    }

    /**
     * 方便函数.
     * 固定置零伤害事件中的以下伤害修饰器:
     * [FREEZING] - 烈焰人等受细雪伤害*5 - 该特性通过给相关生物配置对应元素承伤倍率实现
     * [HARD_HAT] - 头盔减少25%下落的方块伤害 - 该特性无关紧要
     * [ARMOR] - 原版盔甲值/盔甲韧性/魔咒效果组件armor_effectiveness的伤害减免 - Koish 专门的防御力机制已在 [BASE] 中考虑
     * [MAGIC] - 魔咒保护系数伤害减免/女巫85%魔法伤害减免 - Koish 专门的防御力机制已在 [BASE] 中考虑
     */
    private fun EntityDamageEvent.removeUnusedDamageModifiers() {
        modifyDamageModifier(FREEZING, .0)
        modifyDamageModifier(HARD_HAT, .0)
        modifyDamageModifier(ARMOR, .0)
        modifyDamageModifier(MAGIC, .0)
    }

    /**
     * 方便函数.
     * 修改伤害事件中的以下伤害修饰器:
     * [INVULNERABILITY_REDUCTION] - 无懈可击时间伤害减免
     * [BLOCKING] - 格挡
     * [RESISTANCE] - 抗性提升药水效果伤害减免
     * [ABSORPTION] - 伤害吸收
     */
    private fun EntityDamageEvent.modifyUsedDamageModifiers(
        invulnerabilityDamageModifier: Double?,
        blockingDamageModifier: Double?,
        resistanceDamageModifier: Double?,
        absorptionDamageModifier: Double?
    ) {
        invulnerabilityDamageModifier?.let { modifyDamageModifier(INVULNERABILITY_REDUCTION, it) }
        blockingDamageModifier?.let { modifyDamageModifier(BLOCKING, it) }
        resistanceDamageModifier?.let { modifyDamageModifier(RESISTANCE, it) }
        absorptionDamageModifier?.let { modifyDamageModifier(ABSORPTION, it) }
    }

    /**
     * 方便函数.
     * 修改特定原版伤害修饰器.
     */
    private fun EntityDamageEvent.modifyDamageModifier(modifierType: EntityDamageEvent.DamageModifier, value: Double): Boolean {
        if (this.isApplicable(modifierType)) {
            this.setDamage(modifierType, value)
            return true
        }
        return false
    }

    /**
     * 方便函数.
     * 伤害日志.
     */
    private fun EntityDamageEvent.logging(damagee: LivingEntity) {
        LOGGER.info("${damagee.type}(${damagee.uniqueId}) 受到了 $damage 点伤害")

        // 记录聊天
        // 2024/7/25 小米
        // 我承认, 我很复古.
        val message = LinearComponents.linear(
            translatable(damagee.type),
            if (damagee is Player) text(" ${damagee.name} ") else empty(),
            text("受到了"),
            text(" $damage "),
            text("点伤害")
        )
//            .hoverEvent(
//            damageMetadata.damageBundle.packets()
//                .map { packet -> LinearComponents.linear(packet.element.unwrap().displayName, text(": "), text(packet.packetDamage)) }
//                .join(JoinConfiguration.newlines())
//                .let { component ->
//                    HoverEvent.showText(
//                        component
//                            .appendNewline()
//                            .appendNewline()
//                            .append(text("伤害未计算防御阶段"))
//                            .appendNewline()
//                            .append(text("点击复制实体 UUID"))
//                    )
//                }
//        ).clickEvent(
//            ClickEvent.copyToClipboard(damagee.uniqueId.toString())
//        )

        SERVER.filterAudience { it is Player }.sendMessage(message)
    }

    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.registerTrident(event)
    }

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        DamageManager.registerExactArrow(event)
    }

    // 在弹射物击中方块时移除记录的 DamageMetadata.
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (event.hitBlock == null) return
        DamageManager.unregisterProjectile(event.entity)
    }

    // 用于取消自定义伤害的击退.
    @EventHandler
    fun on(event: EntityKnockbackEvent) {
        if (DamageManager.unregisterCancelKnockback(event.entity)) {
            event.isCancelled = true
        }
    }

}

/**
 * 监听 Koish 伤害事件, 使其遵循保护系统的规则.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageIntegration : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PostprocessDamageEvent) {
        val damager = event.damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee as? LivingEntity ?: return
        event.isCancelled = !ProtectionManager.canHurtEntity(damager, damagee, null)
    }

}