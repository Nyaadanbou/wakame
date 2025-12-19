@file:JvmName("LogicImpl")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.damage.DamageManagerImpl.registerExactArrow
import cc.mewcraft.wakame.damage.DamageManagerImpl.registerTrident
import cc.mewcraft.wakame.damage.DamageManagerImpl.registeredCustomDamageMap
import cc.mewcraft.wakame.damage.DamageManagerImpl.registeredProjectileDamageMap
import cc.mewcraft.wakame.damage.mapping.AttackCharacteristicDamageMappings
import cc.mewcraft.wakame.damage.mapping.DamageTypeDamageMappings
import cc.mewcraft.wakame.damage.mapping.NullCausingDamageMappings
import cc.mewcraft.wakame.damage.mapping.PlayerAdhocDamageMappings
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.AttributeMapSnapshot
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.item.behavior.getBehavior
import cc.mewcraft.wakame.item.behavior.impl.weapon.Weapon
import cc.mewcraft.wakame.item.extension.coreContainer
import cc.mewcraft.wakame.item.hasProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.handle
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.serverLevel
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.*
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.potion.PotionEffectType
import xyz.xenondevs.commons.collections.enumSetOf
import xyz.xenondevs.commons.provider.map
import xyz.xenondevs.commons.provider.orElse
import java.time.Duration
import java.util.*
import kotlin.math.min
import kotlin.math.round

// ------------
// 内部实现
// ------------

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

private val DAMAGE_CONFIG = ConfigAccess.INSTANCE["damage/config"]
private val PLAYER_INTRINSIC_ATTACK_COOLDOWN by DAMAGE_CONFIG.optionalEntry<Long>("player_intrinsic_attack_cooldown").orElse(5L).map { it * 50L }
private val EQUIPMENT_CONFIG = DAMAGE_CONFIG.node("equipment")
private val EQUIPMENT_NO_DURABILITY_LOSS_DAMAGE_TYPES by EQUIPMENT_CONFIG.optionalEntry<List<DamageType>>("no_durability_loss_damage_types").orElse(emptyList())
private val EQUIPMENT_AMOUNT_PER_DAMAGE by EQUIPMENT_CONFIG.optionalEntry<Float>("amount_per_damage").orElse(0.25f)
private val EQUIPMENT_MIN_AMOUNT by EQUIPMENT_CONFIG.optionalEntry<Int>("min_amount").orElse(1)
private val EQUIPMENT_MAX_AMOUNT by EQUIPMENT_CONFIG.optionalEntry<Int>("max_amount").orElse(Int.MAX_VALUE)

/**
 * 包含伤害系统的计算逻辑和状态.
 */
internal object DamageManagerImpl : DamageManagerApi {

    // 特殊值, 方便识别. 仅用于触发事件, 以被事件系统监听&修改.
    private const val PLACEHOLDER_DAMAGE_VALUE: Float = 4.95f

    /**
     * 作为 direct_entity 时能够造成伤害的 entity(projectile) 类型.
     */
    private val ENTITY_TYPES_1: Set<EntityType> = enumSetOf(
        EntityType.ARROW,
        EntityType.BREEZE_WIND_CHARGE,
        EntityType.EGG,
        EntityType.ENDER_PEARL,
        EntityType.FIREBALL,
        EntityType.FIREWORK_ROCKET,
        EntityType.LLAMA_SPIT,
        EntityType.SPLASH_POTION,
        EntityType.LINGERING_POTION,
        EntityType.SHULKER_BULLET,
        EntityType.SMALL_FIREBALL,
        EntityType.SNOWBALL,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
        EntityType.WIND_CHARGE,
    )

    /**
     * 作为 direct_entity 时能够对受击者造成伤害的 entity 类型.
     */
    private val ENTITY_TYPES_2: Set<EntityType> = ENTITY_TYPES_1 + enumSetOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
        EntityType.AREA_EFFECT_CLOUD,
    )

    /**
     * 作为 direct_entity 时能够对受击者造成伤害的 entity 类型.
     *
     * 这些实体作为直接实体时, 可能有来源实体, 但视为无源的
     */
    private val ENTITY_TYPES_3: Set<EntityType> = enumSetOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
    )

    /**
     * 需要提前注册 damage_metadata 的、由玩家射出的 entity(projectile) 类型.
     *
     * 这些 projectile 类型的特点是它们的物品形式都可以带上修改伤害的属性.
     *
     * 系统会在这些 projectile 生成时(但还未击中目标之前)根据属性为其注册 damage_metadata.
     * 等到这些 projectile 击中目标后, 将使用已经注册的 damage_metadata 作为计算的依据.
     *
     * 如此设计是为了让玩家打出的 projectile 在刚离开玩家时, 其最终将要造成的伤害就已经确定.
     */
    private val ENTITY_TYPES_4: Set<EntityType> = enumSetOf(
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
    )

    /**
     * 无来源实体时, 伤害需要考虑物品属性的直接实体类型.
     *
     * 例如: 发射器射出带自定义属性的自定义箭矢.
     */
    private val ENTITY_TYPES_5: Set<EntityType> = enumSetOf(
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW
    )

    /**
     * 对 [victim] 造成由 [metadata] 指定的萌芽伤害.
     *
     * @return 是否真的造成了伤害及其附加效果. 诸如生物无敌, 免疫该伤害, 伤害事件被取消等情况会返回 false.
     */
    override fun hurt(
        victim: LivingEntity,
        metadata: DamageMetadata,
        source: DamageSource,
        knockback: Boolean,
    ): Boolean {
        victim.registerDamage(metadata)

        // 如果自定义伤害有源且需要取消击退.
        // 无源伤害 (不存在来源实体) 不会触发击退事件.
        if (!knockback && source.causingEntity != null) {
            victim.registerCancelKnockback()
        }

        // 直接调用nms方法造成伤害.
        // 原因是只有nms的方法会返回布尔值, bukkit方法直接吃掉了布尔值.
        return victim.handle.hurtServer(victim.world.serverLevel, source.handle, PLACEHOLDER_DAMAGE_VALUE)
    }

    /**
     * 判定某次伤害是否会使盔甲损失耐久度.
     */
    override fun bypassesHurtEquipment(damageType: DamageType): Boolean {
        return EQUIPMENT_NO_DURABILITY_LOSS_DAMAGE_TYPES.contains(damageType)
    }

    /**
     * 计算单次伤害下盔甲的耐久损失.
     */
    override fun computeEquipmentHurtAmount(damageAmount: Float): Int {
        return (damageAmount * EQUIPMENT_AMOUNT_PER_DAMAGE).toInt().coerceIn(EQUIPMENT_MIN_AMOUNT, EQUIPMENT_MAX_AMOUNT).coerceAtLeast(0)
    }

    /**
     * 包含所有与 Bukkit 伤害事件交互的逻辑.
     */
    override fun injectDamageLogic(event: EntityDamageEvent, originLastHurt: Float, isDuringInvulnerable: Boolean): Float {
        val finalDamageContext = handleKoishDamageCalculationLogic(event)
        if (finalDamageContext == null) {
            // Bukkit 伤害事件被取消, 后续服务端中的 actuallyHurt 方法会返回false
            // LivingEntity#lastHurt 变量其实不会被设置, 保险起见返回 originLastHurt
            event.isCancelled = true
            return originLastHurt
        } else {
            val finalDamage = finalDamageContext.finalDamageMap.values.sum().toFloat()
            val damagee = event.entity
            // 无懈可击期间只会受到大于 lastHurt 的伤害
            if (isDuringInvulnerable && damagee is LivingEntity) {
                if (finalDamage <= damagee.lastDamage) {
                    event.isCancelled = true
                    // 同上保险起见返回 originLastHurt
                    return originLastHurt
                }
            }

            // 触发 PostprocessDamageEvent 事件
            val postprocessEvent = PostprocessDamageEvent(finalDamageContext, event)
            if (!postprocessEvent.callEvent()) {
                // Koish 伤害事件被取消, 则直接返回
                // Koish 伤害事件被取消时, 其内部的 Bukkit 伤害事件必然是取消的状态
                // 同上保险起见返回 originLastHurt
                return originLastHurt
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
            return finalDamage
        }
    }

    /**
     * Koish 伤害系统对伤害进行修改的逻辑.
     *
     * 返回 `null` 意思是伤害事件应该要被取消.
     */
    private fun handleKoishDamageCalculationLogic(event: EntityDamageEvent): FinalDamageContext? {
        // 只处理伤害承受者是生物的情况
        // TODO 考虑非生物
        val damagee = event.entity as? LivingEntity ?: return null

        // 提取 Bukkit 伤害事件中的有用信息生成上下文
        val rawDamageContext = RawDamageContext(event)

        // 计算攻击阶段的伤害信息
        // 考虑伤害发起者对伤害值的各种影响
        // 为空时取消伤害事件
        val damageMetadata = createAttackPhaseMetadata(rawDamageContext) ?: return null

        // 计算防御阶段的伤害信息
        // 考虑伤害承受者对伤害值的各种影响
        // 为空时取消伤害事件
        val defenseMetadata = createDefensePhaseMetadata(rawDamageContext) ?: return null

        // 计算最终伤害的信息
        val finalDamageContext = createFinalDamageContext(damageMetadata, defenseMetadata)
        return finalDamageContext
    }

    /**
     * 计算“攻击阶段”的伤害信息, 即计算*造成*伤害的一方对结果的影响.
     *
     * @return 攻击阶段的伤害信息, null意为取消伤害事件
     */
    private fun createAttackPhaseMetadata(context: RawDamageContext): DamageMetadata? {
        val damagee = context.damagee
        val customDamageMetadata = damagee.getRegisteredDamage()
        if (customDamageMetadata != null) {
            // 该伤害是一个自定义伤害(由代码执行 hurt 造成的),
            // 直接返回已经注册的自定义伤害信息
            damagee.unregisterDamage() // 注销, 因为已经“用掉”了
            return customDamageMetadata
        }

        val damageSource = context.damageSource
        val directEntity = damageSource.directEntity
        if (directEntity == null) {
            // 不存在 direct_entity, 例如: 自然伤害(溺水、岩浆)
            // 使用 damage_type_mappings, 根据 damage_type 生成 damage_metadata
            return context.toDamageMetadata(Mapping.DAMAGE_TYPE)
        }

        val causingEntity = damageSource.causingEntity
        when (causingEntity) { // 根据 causing_entity 的类型, 生成 damage_metadata

            null -> { // 不存在 causing_entity
                if (directEntity.type in ENTITY_TYPES_5) {
                    // 特殊处理属于 attributed_arrow_types 的 direct_entity, 考虑箭矢本身给予的额外伤害.
                    // 例如: 无源箭矢(发射器)
                    return createNoCausingAttributedArrowDamage(directEntity as AbstractArrow) ?: context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                } else if (directEntity.type in ENTITY_TYPES_2) {
                    // 使用映射来计算 direct_entity 造成的伤害.
                    // attributed_arrow_types 中的直接实体类型不会进入此分支
                    // 例如: 无源弹射物(发射器), 无源TNT(发射器), 无源区域效果云(发射器), 无源末影水晶爆炸.
                    return context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                } else {
                    // 不太可能发生
                    return createDefaultDamage(context)
                }
            }

            is Player -> { // causing_entity 是 player
                if (directEntity.type in ENTITY_TYPES_4) {
                    // 这里的情况: direct_entity 的 *类型* 在已注册弹射物类型当中.
                    // 这意味着 direct_entity 在之前的某个时间点*也许*已经注册了一个伤害信息.
                    // 先尝试获取已经注册的伤害信息
                    // 若没有注册则考虑弹射物物品上的属性.
                    // 再没有则使用 player_adhoc_mappings.
                    // 例如: 玩家箭矢, 玩家光灵箭, 玩家三叉戟
                    return createRegisteredAbstractArrowDamage(directEntity as AbstractArrow) ?: context.toDamageMetadata(Mapping.PLAYER_ADHOC)
                } else if (directEntity.type in ENTITY_TYPES_2) {
                    // 这里的情况: direct_entity 属于游戏里比较特殊的能够造成伤害的实体.
                    // 直接使用 player_adhoc_mappings.
                    // 例如: 玩家其他弹射物, 玩家TNT, 玩家末影水晶
                    return context.toDamageMetadata(Mapping.PLAYER_ADHOC)
                }

                return createPlayerAttackDamage(context)
            }

            is LivingEntity -> { // causing_entity 是 non-player living_entity
                if (directEntity.type in ENTITY_TYPES_3) {
                    // causing_entity 是 living_entity 但 direct_entity
                    // 是 tnt_primed 等特殊情况时, 视为没有 causing_entity
                    // 例如: 非玩家生物点燃的TNT, 非玩家生物引爆的末影水晶
                    return context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                }

                return context.toDamageMetadata(Mapping.ATTACK_CHARACTERISTIC)
            }

            is FallingBlock -> { // causing_entity 是非 living_entity 的下落的方块
                // 根据伤害类型计算伤害.
                return context.toDamageMetadata(Mapping.DAMAGE_TYPE)
            }

            else -> { // 不太可能发生, 除非有插件在编造一些不太合法的 DamageSource
                return createDefaultDamage(context)
            }

        }
    }

    /**
     * 计算“防御阶段”的伤害信息, 即计算*承受伤害*的一方对结果的影响.
     *
     * @return 防御阶段的伤害信息, null意为取消伤害事件
     */
    private fun createDefensePhaseMetadata(context: RawDamageContext): DefenseMetadata? {
        val damagee = context.damagee
        val damageeAttributes = AttributeMapAccess.INSTANCE.get(damagee).getOrElse {
            LOGGER.warn("Failed to generate defense metadata because the entity $damagee does not have an attribute map.")
            return null
        }

        // 受伤者抗性提升状态效果等级
        val resistanceLevel = damagee.getPotionEffect(PotionEffectType.RESISTANCE)?.amplifier?.plus(1) ?: 0

        // 受伤者是否格挡
        // 只有玩家有格挡机制
        return if (damagee is Player) {
            DefenseMetadata(damageeAttributes, resistanceLevel, damagee.isBlocking)
        } else {
            DefenseMetadata(damageeAttributes, resistanceLevel)
        }
    }

    /**
     * 计算并创建 [FinalDamageContext].
     */
    private fun createFinalDamageContext(damageMetadata: DamageMetadata, defenseMetadata: DefenseMetadata): FinalDamageContext {
        val baseModifierValueMap = calculateBaseModifierValueMap(damageMetadata, defenseMetadata)
        val blockingModifierValueMap = calculateBlockingModifierValueMap(damageMetadata, defenseMetadata, baseModifierValueMap)
        val resistanceModifierValue = calculateResistanceModifierValue(damageMetadata)
        val absorptionModifierValue = calculateAbsorptionModifierValue(damageMetadata)

        val finalDamageMap = calculateFinalDamageMap(baseModifierValueMap, blockingModifierValueMap)
        return FinalDamageContext(
            damageMetadata, defenseMetadata, baseModifierValueMap.values.sum(), blockingModifierValueMap.values.sum(), resistanceModifierValue, absorptionModifierValue, finalDamageMap
        )
    }


    /**
     * 计算各元素的基础伤害, 主要与 Koish 的属性和元素系统有关. 在功能上包含了原版的 [ARMOR] 和 [MAGIC] 修饰器对伤害的影响.
     */
    private fun calculateBaseModifierValueMap(
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata,
    ): Reference2DoubleMap<RegistryEntry<Element>> {
        val map = Reference2DoubleOpenHashMap<RegistryEntry<Element>>()
        val damagePackets = damageMetadata.damageBundle.packets()
        // 对空伤害包发出警告
        if (damagePackets.isEmpty()) {
            LOGGER.warn("Empty damage bundle! There may be some problems with the calculation of damage!", IllegalStateException())
        }
        damagePackets.forEach { damagePacket ->
            val elementType = damagePacket.element
            val elementDamage = calculateElementDamage(damageMetadata, defenseMetadata, damagePacket)
            // 忽略修饰后为0的元素伤害
            if (elementDamage > 0) {
                map[elementType] = elementDamage
            }
        }
        return map
    }

    /**
     * 计算特定元素的伤害.
     */
    private fun calculateElementDamage(
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata,
        damagePacket: DamagePacket,
    ): Double {
        // 伤害包的元素类型
        val elementType = damagePacket.element
        // 原始伤害
        var damage = damagePacket.packetDamage

        // 基于攻击者的伤害修饰计算:
        // 攻击者元素伤害倍率(或称攻击威力)修饰
        // 直接倍乘
        damage *= damagePacket.rate
        // 攻击者暴击倍率修饰
        // 直接倍乘
        damage *= damageMetadata.criticalStrikeMetadata.power

        // 基于受伤者的伤害修饰计算:
        // 受伤者防御力修饰
        // 受伤者对应元素防御值, 不会小于0
        val defense = defenseMetadata.getElementDefense(elementType)
        // 有效防御值根据配置文件的公式计算
        val validDefense = DamageRules.calculateValidDefense(
            defense = defense,
            defensePenetration = damagePacket.defensePenetration,
            defensePenetrationRate = damagePacket.defensePenetrationRate
        )
        // 防御后伤害值根据配置文件公式计算
        damage = DamageRules.calculateDamageAfterDefense(
            originalDamage = damage,
            validDefense = validDefense
        )
        // 受伤者承伤倍率修饰
        // 受伤者对应元素承伤倍率值
        // 直接倍乘
        val incomingDamageRate = defenseMetadata.getElementIncomingDamageRate(elementType)
        damage *= incomingDamageRate

        // 伤害系统底层机制修饰计算:
        // 最小伤害限制修饰
        val leastDamage = if (damagePacket.packetDamage > 0) DamageRules.LEAST_DAMAGE else 0.0
        damage = damage.coerceAtLeast(leastDamage)
        // 伤害取整修饰
        if (DamageRules.ROUNDING_DAMAGE) {
            damage = round(damage)
        }

        return damage
    }

    /**
     * 计算格挡对各元素伤害的修饰.
     */
    private fun calculateBlockingModifierValueMap(
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata,
        baseDamageMap: Reference2DoubleMap<RegistryEntry<Element>>,
    ): Reference2DoubleMap<RegistryEntry<Element>> {
        val map = Reference2DoubleOpenHashMap<RegistryEntry<Element>>()
        val elements = baseDamageMap.keys
        if (defenseMetadata.isBlocking && !damageMetadata.ignoreBlocking) {
            val blockingDamageReductionMap = defenseMetadata.blockingDamageReductionMap
            elements.forEach { element ->
                val baseDamage = baseDamageMap.getOrDefault(element, 0.0)
                val blockingDamageReduction = blockingDamageReductionMap.getOrDefault(element, 0.0)
                val blockingDamage = -min(baseDamage, blockingDamageReduction)
                if (blockingDamage < 0) {
                    map.put(element, blockingDamage)
                }
            }
        }
        return map
    }

    /**
     * 计算原版抗性提升状态效果对伤害的修饰.
     *
     * 不区分元素.
     *
     * TODO 抗性提升机制
     */
    private fun calculateResistanceModifierValue(
        damageMetadata: DamageMetadata,
    ): Double {
        return .0
    }

    /**
     * 计算原版伤害吸收(黄心)机制对伤害的修饰.
     *
     * 不区分元素.
     */
    private fun calculateAbsorptionModifierValue(
        damageMetadata: DamageMetadata,
    ): Double? {
        return if (damageMetadata.ignoreAbsorption) 0.0 else null
    }

    /**
     * 计算各元素最终伤害.
     */
    private fun calculateFinalDamageMap(
        baseDamageMap: Reference2DoubleMap<RegistryEntry<Element>>,
        blockingDamageMap: Reference2DoubleMap<RegistryEntry<Element>>,
    ): Reference2DoubleMap<RegistryEntry<Element>> {
        val finalDamageMap = Reference2DoubleOpenHashMap<RegistryEntry<Element>>()

        val elements = baseDamageMap.keys

        for (element in elements) {
            val sum = baseDamageMap.getOrDefault(element, 0.0) +
                    blockingDamageMap.getOrDefault(element, 0.0)
            finalDamageMap.put(element, sum)
        }

        return finalDamageMap
    }

    /**
     * 方便函数.
     *
     * 固定置零伤害事件中的以下伤害修饰器:
     * - [INVULNERABILITY_REDUCTION] - 无懈可击期间的伤害减免 - 为了元素可合适的作为伤害组分而移除. 由于小于等于 `lastHurt` 的伤害不会触发伤害事件, 故移除此修饰器不会导致高频伤害失去保护.
     * - [FREEZING] - 烈焰人等受细雪伤害*5 - 该特性通过给相关生物配置对应元素承伤倍率实现
     * - [HARD_HAT] - 头盔减少25%下落的方块伤害 - 该特性无关紧要
     * - [ARMOR] - 原版盔甲值/盔甲韧性/魔咒效果组件 `armor_effectiveness` 的伤害减免 - Koish 专门的防御力机制已在 [BASE] 中考虑
     * - [MAGIC] - 魔咒保护系数伤害减免/女巫85%魔法伤害减免 - Koish 专门的防御力机制已在 [BASE] 中考虑
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
     *
     * 修改伤害事件中的以下伤害修饰器:
     * - [BLOCKING] - 格挡
     * - [RESISTANCE] - 抗性提升药水效果伤害减免
     * - [ABSORPTION] - 伤害吸收
     */
    private fun EntityDamageEvent.modifyUsedDamageModifiers(
        finalDamageContext: FinalDamageContext,
    ) {
        finalDamageContext.blockingModifierValue?.let { modifyDamageModifier(BLOCKING, it) }
        finalDamageContext.resistanceModifierValue?.let { modifyDamageModifier(RESISTANCE, it) }
        finalDamageContext.absorptionModifierValue?.let { modifyDamageModifier(ABSORPTION, it) }
    }

    /**
     * 方便函数.
     *
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
     *
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

    private enum class Mapping {
        DAMAGE_TYPE,
        PLAYER_ADHOC,
        NULL_CAUSING_ENTITY,
        ATTACK_CHARACTERISTIC,
    }

    private fun RawDamageContext.toDamageMetadata(mapping: Mapping): DamageMetadata {
        when (mapping) {
            Mapping.DAMAGE_TYPE -> {
                val damageMapper = DamageTypeDamageMappings.get(this.damageSource.damageType)
                return damageMapper.generate(this)
            }

            Mapping.PLAYER_ADHOC -> {
                val damageMapper = PlayerAdhocDamageMappings.get(this)
                return damageMapper?.generate(this) ?: run {
                    LOGGER.warn("Cannot find a player-adhoc mapper for context: $this. Using default damage metadata.")
                    return VanillaDamageMetadata(this.damage)
                }
            }

            Mapping.NULL_CAUSING_ENTITY -> {
                val damageMapper = NullCausingDamageMappings.get(this)
                return damageMapper?.generate(this) ?: run {
                    LOGGER.warn("Cannot find a null-causing mapper for context: $this. Using default damage metadata.")
                    return VanillaDamageMetadata(this.damage)
                }
            }

            Mapping.ATTACK_CHARACTERISTIC -> {
                val damageMapper = AttackCharacteristicDamageMappings.get(this)
                return damageMapper?.generate(this) ?: run {
                    // 配置文件未指定该情景下生物的伤害映射,
                    // 这种情况返回原版伤害值、默认元素、无防穿、无暴击
                    LOGGER.warn("Cannot find a attack-characteristic mapper for context: $this. Using default damage metadata.")
                    val damage = this.damage
                    val damageBundle = damageBundle {
                        default {
                            min(damage)
                            max(damage)
                            rate(1.0)
                            defensePenetration(0.0)
                            defensePenetrationRate(0.0)
                        }
                    }
                    return EntityDamageMetadata(
                        damageBundle = damageBundle,
                        criticalStrikeMetadata = CriticalStrikeMetadata.NONE
                    )
                }
            }
        }
    }

    // 可以返回 null, 意为取消本次伤害
    private fun createPlayerAttackDamage(context: RawDamageContext): DamageMetadata? {
        val player = context.damageSource.causingEntity as? Player ?: error("The causing entity must be a player.")
        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return createPlayerIntrinsicAttackDamage(player)
        val weapon = itemstack.getBehavior<Weapon>() ?: return createPlayerIntrinsicAttackDamage(player)
        return weapon.generateDamageMetadata(player, itemstack)
    }

    // 玩家空手攻击处于冷却时返回 null, 使伤害事件取消
    private fun createPlayerIntrinsicAttackDamage(player: Player): DamageMetadata? {
        if (player.canIntrinsicAttack()) {
            player.markIntrinsicAttack()
            return PlayerDamageMetadata.INTRINSIC_ATTACK
        } else {
            return null
        }
    }

    private fun createDefaultDamage(context: RawDamageContext): DamageMetadata {
        val damagee = context.damagee
        val directEntity = context.damageSource.directEntity
        val causingEntity = context.damageSource.causingEntity
        LOGGER.warn("Why can ${causingEntity?.type} cause damage to ${damagee.type} through ${directEntity?.type}? This should not happen.")
        return VanillaDamageMetadata(context.damage)
    }

    /**
     * 将 [projectile] 已注册的伤害注销.
     *
     * 该函数应该在弹射物不再可能造成伤害时调用.
     */
    fun unregisterProjectile(projectile: Projectile) {
        projectile.unregisterDamage()
    }

    /**
     * 为 [event] 中的弹射物注册伤害.
     *
     * 该函数应该在弹射物即将离开玩家时调用.
     * 目前只注册玩家的 [Trident].
     *
     * 伤害过期的情况如下 (满足其一):
     * - 超过有效期 (30秒)
     * - 击中方块
     *
     * @see registerExactArrow 注册箭矢和光灵箭矢的伤害
     */
    fun registerTrident(event: ProjectileLaunchEvent) {
        val trident: Trident = event.entity as? Trident ?: return
        val shooter: Player = trident.shooter as? Player ?: return
        val attributes = shooter.attributeContainer.getSnapshot()
        trident.registerDamage(RegisteredProjectileDamage(1.0, attributes))
    }

    /**
     * 为 [event] 中的弹射物注册伤害.
     *
     * 该函数应该在玩家通过拉弓/弩射出弹射物时调用.
     * 目前只注册玩家的 [Arrow] 和 [SpectralArrow].
     *
     * 因为玩家射出的箭矢伤害需要根据拉弓的力度进行调整, 所以监听此事件而非 [ProjectileLaunchEvent].
     *
     * 伤害元数据过期的情况如下 (满足其一):
     * - 超过有效期 (30秒)
     * - 击中方块
     *
     * @see registerTrident 注册三叉戟的伤害
     */
    fun registerExactArrow(event: EntityShootBowEvent) {
        val exactArrow: AbstractArrow = event.projectile as? AbstractArrow ?: return
        val shooter: Player = event.entity as? Player ?: return
        val force: Double = when (event.bow?.type) {
            Material.BOW -> DamageRules.calculateBowForce(shooter.activeItemUsedTime) // 玩家射出的箭矢伤害需要根据拉弓的力度进行调整
            Material.CROSSBOW -> 1.0
            else -> return
        }
        val attributes = shooter.attributeContainer.getSnapshot()
        exactArrow.registerDamage(RegisteredProjectileDamage(force, attributes))
    }

    /**
     * 该函数应该在弹射物击中实体时调用.
     *
     * 考虑弹射物物品上的属性.
     *
     * 返回 `null` 表示不由该函数负责.
     */
    private fun createNoCausingAttributedArrowDamage(arrow: AbstractArrow): DamageMetadata? {
        val itemstack = arrow.itemStack
        if (!itemstack.hasProp(ItemPropTypes.ARROW)) return null
        val itemcores = itemstack.coreContainer ?: return null
        val modifiersOnArrow = itemcores.collectAttributeModifiers(itemstack, ItemSlot.imaginary())

        val arrowAttributes = getImaginaryArrowAttributes() ?: return null
        arrowAttributes.addTransientModifiers(modifiersOnArrow)

        val damageBundle = damageBundle(arrowAttributes) {
            every {
                standard()
            }
        }
        return VanillaDamageMetadata(damageBundle)
    }

    /**
     * 该函数应该在弹射物击中实体时调用.
     *
     * 先尝试获取已经注册的伤害信息, 若没有注册则考虑弹射物物品上的属性.
     *
     * 返回 `null` 表示不由该函数负责.
     */
    private fun createRegisteredAbstractArrowDamage(abstractArrow: AbstractArrow): DamageMetadata? {
        val registeredProjectileDamage = abstractArrow.getRegisteredDamage()
        val attributes = registeredProjectileDamage?.attributes ?: getImaginaryArrowAttributes() ?: return null
        val force = registeredProjectileDamage?.force ?: 1.0
        val damageBundle = run {
            val itemstack = abstractArrow.itemStack
            if (!itemstack.hasProp(ItemPropTypes.ARROW)) return null
            val itemcores = itemstack.coreContainer ?: return null
            val modifiersOnArrow = itemcores.collectAttributeModifiers(itemstack, ItemSlot.imaginary())

            attributes.addTransientModifiers(modifiersOnArrow)
            damageBundle(attributes) {
                every {
                    standard()
                    min { force * standard() }
                    max { force * standard() }
                }
            }
        }
        return PlayerDamageMetadata(
            attributes = attributes,
            damageBundle = damageBundle
        )
    }

    private fun getImaginaryArrowAttributes(): AttributeMapSnapshot? {
        return BuiltInRegistries.IMG_ATTRIBUTE_MAP["minecraft:arrow"]?.getSnapshot() ?: run {
            LOGGER.warn("Could not find imaginary attribute map \"minecraft:arrow\" for context: $this. Returning default 1.0 damage metadata.")
            null
        }
    }

    /**
     * 包含已注册的 [DamageMetadata], 用于让代码对实体发起任意伤害.
     *
     * ## 注意
     * 不要跟 [registeredProjectileDamageMap] 搞混了, 这里的伤害基本来源于由代码额外造成的伤害(比如: 武器攻击特效, MythicMobs Mechanic).
     */
    private val registeredCustomDamageMap: Cache<UUID, DamageMetadata> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .build()

    private fun Entity.getRegisteredDamage(): DamageMetadata? {
        return registeredCustomDamageMap.getIfPresent(uniqueId)
    }

    private fun Entity.registerDamage(damageMetadata: DamageMetadata) {
        registeredCustomDamageMap.put(uniqueId, damageMetadata)
    }

    private fun Entity.unregisterDamage() {
        registeredCustomDamageMap.invalidate(uniqueId)
    }

    /**
     * 包含已注册的 [Projectile] 的 [AttributeMapSnapshot], 用于在弹射物刚被创建时就固定其伤害(减免前).
     *
     * ## 注意
     * 不要跟 [registeredCustomDamageMap] 搞混了, 这里的属性快照仅仅用于实现弹射物的伤害计算.
     * 这样设计可以让弹射物的属性在*打中*实体时被拦截和修改, 允许代码根据受伤实体的状态来修改属性;
     * 而不是*创建*弹射物时拦截属性和修改 - 因为代码无法从这个时机得知受伤实体的状态.
     */
    private val registeredProjectileDamageMap: Cache<UUID, RegisteredProjectileDamage> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .build()

    private fun Projectile.getRegisteredDamage(): RegisteredProjectileDamage? {
        return registeredProjectileDamageMap.getIfPresent(uniqueId)
    }

    private fun Projectile.registerDamage(damage: RegisteredProjectileDamage) {
        registeredProjectileDamageMap.put(uniqueId, damage)
    }

    private fun Projectile.unregisterDamage() {
        registeredProjectileDamageMap.invalidate(uniqueId)
    }

    /**
     * 弹射物的伤害信息, 打包在一起方便组织代码.
     */
    private data class RegisteredProjectileDamage(val force: Double, val attributes: AttributeMapSnapshot)

    /**
     * 包含需要被取消击退的 [Entity.getUniqueId], 用于取消自定义伤害造成的击退效果.
     */
    private val entitiesCancellingKnockback: HashSet<UUID> = HashSet()

    private fun Entity.registerCancelKnockback() {
        entitiesCancellingKnockback.add(uniqueId)
    }

    fun unregisterCancelKnockback(entity: Entity): Boolean {
        return entitiesCancellingKnockback.remove(entity.uniqueId)
    }

    /**
     * 包含玩家上一次空手攻击(手持非 Weapon 物品时左键)的时间戳.
     */
    private val playerIntrinsicAttackTimestampMap: HashMap<UUID, Long> = HashMap()

    /**
     * 记录玩家空手攻击的时间戳.
     */
    private fun Player.markIntrinsicAttack() {
        playerIntrinsicAttackTimestampMap.put(uniqueId, System.currentTimeMillis())
    }

    /**
     * 玩家此时是否可以进行空手攻击.
     */
    private fun Player.canIntrinsicAttack(): Boolean {
        return System.currentTimeMillis() - playerIntrinsicAttackTimestampMap.getOrDefault(uniqueId, 0L) >= PLAYER_INTRINSIC_ATTACK_COOLDOWN
    }

}
