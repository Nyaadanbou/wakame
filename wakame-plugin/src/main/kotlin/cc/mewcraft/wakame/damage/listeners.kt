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
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
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
        val rawDamageContext = RawDamageContext(event)

        // 计算攻击阶段的伤害信息
        // 考虑伤害发起者对伤害值的各种影响
        // 伤害信息为空时取消伤害事件
        val damageMetadata = DamageManager.createAttackPhaseMetadata(rawDamageContext) ?: run {
            event.isCancelled = true
            return
        }

        // 计算防御阶段的伤害信息
        // 考虑伤害承受者对伤害值的各种影响
        // 伤害信息为空时取消伤害事件
        val defenseMetadata = DamageManager.createDefensePhaseMetadata(rawDamageContext) ?: run {
            event.isCancelled = true
            return
        }

        // 计算最终伤害的信息
        val finalDamageContext = DamageManager.createFinalDamageContext(damageMetadata, defenseMetadata)

        // 触发 PostprocessDamageEvent 事件
        val postprocessEvent = PostprocessDamageEvent(finalDamageContext, event)
        if (!postprocessEvent.callEvent()) {
            // Koish 伤害事件被取消, 则直接返回
            // Koish 伤害事件被取消时, 其内部的 Bukkit 伤害事件必然是取消的状态
            return
        }

        // 修改 BASE 伤害
        // 由于原版中某些伤害附带效果只能通过相应修饰器实现, 如增加相应统计信息/扣除黄心等
        // 故不能将伤害计算全赶到 BASE 中, 必须考虑某些原版伤害修饰器
        event.modifyDamageModifier(BASE, finalDamageContext.baseModifierValue)
        // 置零 Koish 伤害系统不考虑的原版伤害修饰器
        event.removeUnusedDamageModifiers()
        // 修改 Koish 伤害系统考虑的原版伤害修饰器
        event.modifyUsedDamageModifiers(finalDamageContext)

        // 记录日志
        if (LOGGING) postprocessEvent.logging()
    }

    /**
     * 方便函数.
     * 固定置零伤害事件中的以下伤害修饰器:
     * [INVULNERABILITY_REDUCTION] - 无懈可击期间的伤害减免 - 为了元素可合适的作为伤害组分而移除. 由于小于等于 lastHurt 的伤害不会触发伤害事件, 故移除此修饰器不会导致高频伤害失去保护.
     * [FREEZING] - 烈焰人等受细雪伤害*5 - 该特性通过给相关生物配置对应元素承伤倍率实现
     * [HARD_HAT] - 头盔减少25%下落的方块伤害 - 该特性无关紧要
     * [ARMOR] - 原版盔甲值/盔甲韧性/魔咒效果组件armor_effectiveness的伤害减免 - Koish 专门的防御力机制已在 [BASE] 中考虑
     * [MAGIC] - 魔咒保护系数伤害减免/女巫85%魔法伤害减免 - Koish 专门的防御力机制已在 [BASE] 中考虑
     */
    private fun EntityDamageEvent.removeUnusedDamageModifiers() {
        modifyDamageModifier(INVULNERABILITY_REDUCTION, .0)
        modifyDamageModifier(FREEZING, .0)
        modifyDamageModifier(HARD_HAT, .0)
        modifyDamageModifier(ARMOR, .0)
        modifyDamageModifier(MAGIC, .0)
    }

    /**
     * 方便函数.
     * 修改伤害事件中的以下伤害修饰器:
     * [BLOCKING] - 格挡
     * [RESISTANCE] - 抗性提升药水效果伤害减免
     * [ABSORPTION] - 伤害吸收
     */
    private fun EntityDamageEvent.modifyUsedDamageModifiers(
        finalDamageContext: FinalDamageContext
    ) {
        finalDamageContext.blockingModifierValue?.let { modifyDamageModifier(BLOCKING, it) }
        finalDamageContext.resistanceModifierValue?.let { modifyDamageModifier(RESISTANCE, it) }
        finalDamageContext.absorptionModifierValue?.let { modifyDamageModifier(ABSORPTION, it) }
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
    private fun PostprocessDamageEvent.logging() {
        LOGGER.info("${damagee.type}(${damagee.uniqueId}) 受到了 $finalDamage 点伤害")

        val power = damageMetadata.criticalStrikeMetadata.power
        val attackPhaseComponent = text("原始伤害:")
            .appendNewline()
            .append(
                damageMetadata.damageBundle.packets()
                    .map { packet -> LinearComponents.linear(text(" - "), packet.element.unwrap().displayName, text(": "), text(packet.packetDamage)) }
                    .join(JoinConfiguration.newlines()))
            .appendNewline()
            .append(
                when (damageMetadata.criticalStrikeMetadata.state) {
                    CriticalStrikeState.POSITIVE -> text("暴击: 正(x$power)")
                    CriticalStrikeState.NEGATIVE -> text("暴击: 负(x$power)")
                    CriticalStrikeState.NONE -> text("暴击: 无(x$power)")
                }
            )
            .appendNewline()
            .append(text("忽略格挡: ${if (damageMetadata.ignoreBlocking) "是" else "否"}"))
            .appendNewline()
            .append(text("忽略抗性提升: ${if (damageMetadata.ignoreResistance) "是" else "否"}"))
            .appendNewline()
            .append(text("忽略伤害吸收: ${if (damageMetadata.ignoreAbsorption) "是" else "否"}"))


        val defensePhaseComponent = text("受伤者防御:")
            .appendNewline()
            .append(
                defenseMetadata.defenseMap
                    .map { (element, value) -> LinearComponents.linear(text(" - "), element.unwrap().displayName, text(": "), text(value)) }
                    .join(JoinConfiguration.newlines())
            )
            .appendNewline()
            .appendNewline()
            .append(text("受伤者承伤倍率:"))
            .appendNewline()
            .append(
                defenseMetadata.incomingDamageRateMap
                    .map { (element, value) -> LinearComponents.linear(text(" - "), element.unwrap().displayName, text(": "), text(value)) }
                    .join(JoinConfiguration.newlines())
            )

        val finalDamageComponent = text("最终伤害:")
            .appendNewline()
            .append(
                finalDamageContext.finalDamageMap
                    .map { (element, value) -> LinearComponents.linear(text(" - "), element.unwrap().displayName, text(": "), text(value)) }
                    .join(JoinConfiguration.newlines())
            )
            .appendNewline()
            .append(text("基础伤害: ${finalDamageContext.baseModifierValue}"))
            .appendNewline()
            .append(text("格挡伤害: ${finalDamageContext.blockingModifierValue}"))
            .appendNewline()
            .append(text("抗性伤害: ${finalDamageContext.resistanceModifierValue}"))
            .appendNewline()
            .append(text("吸收伤害: ${finalDamageContext.absorptionModifierValue}"))

        val message = LinearComponents.linear(
            translatable(damagee.type)
                .append(if (damagee is Player) text("(${damagee.name})") else empty())
                .clickEvent(ClickEvent.copyToClipboard(damagee.uniqueId.toString())),
            text("受到了 $finalDamage 点伤害").hoverEvent(finalDamageComponent),
            text(" ("),
            text("攻击阶段").decorate(TextDecoration.UNDERLINED).hoverEvent(attackPhaseComponent),
            text("|"),
            text("防御阶段").decorate(TextDecoration.UNDERLINED).hoverEvent(defensePhaseComponent),
            text(")")
        )

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