@file:JvmName("LogicImpl")
@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.DamageManager.registerExactArrow
import cc.mewcraft.wakame.damage.DamageManager.registerTrident
import cc.mewcraft.wakame.damage.DamageManager.registeredCustomDamageMetadata
import cc.mewcraft.wakame.damage.DamageManager.registeredProjectileDamages
import cc.mewcraft.wakame.damage.mapping.AttackCharacteristicDamageMappings
import cc.mewcraft.wakame.damage.mapping.DamageTypeDamageMappings
import cc.mewcraft.wakame.damage.mapping.NullCausingDamageMappings
import cc.mewcraft.wakame.damage.mapping.PlayerAdhocDamageMappings
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.AttributeMapSnapshot
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Weapon
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getBehavior
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.hasBehaviorExact
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.RecursionGuard
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import org.bukkit.Material
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.SpectralArrow
import org.bukkit.entity.Trident
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.*
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.potion.PotionEffectType
import xyz.xenondevs.commons.collections.enumSetOf
import java.time.Duration
import java.util.*
import kotlin.math.round

// ------------
// 内部实现
// ------------

/**
 * 包含伤害系统的计算逻辑和状态.
 */
internal object DamageManager : DamageManagerApi {

    // 特殊值, 方便识别. 仅用于触发事件, 以被事件系统监听&修改.
    private const val PLACEHOLDER_DAMAGE_VALUE = 4.95

    /**
     * 作为 direct_entity 时能够造成伤害的 projectile 类型.
     */
    private val PROJECTILE_DAMAGER_TYPES: Set<EntityType> = enumSetOf(
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
    private val DIRECT_ATTACKABLE_TYPES: Set<EntityType> = PROJECTILE_DAMAGER_TYPES + enumSetOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
        EntityType.AREA_EFFECT_CLOUD,
    )

    /**
     * 作为 direct_entity 时能够对受击者造成伤害的 entity 类型.
     * 这些实体作为直接实体时, 可能有来源实体, 但视为无源的
     */
    private val AS_NULL_CAUSING_DIRECT_ATTACKABLE_TYPES: Set<EntityType> = enumSetOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
    )

    /**
     * 需要提前注册 damage_metadata 的 projectile 类型 (由玩家射出的).
     * 这些 projectile 类型的特点是它们的物品形式都可以带上修改伤害的属性.
     *
     * 系统会在这些 projectile 生成时(但还未击中目标之前)根据属性为其注册 damage_metadata.
     * 等到这些 projectile 击中目标后, 将使用已经注册的 damage_metadata 作为计算的依据.
     *
     * 如此设计是为了让玩家打出的 projectile 在刚离开玩家时, 其最终将要造成的伤害就已经确定.
     */
    private val REGISTERED_PROJECTILE_TYPES: Set<EntityType> = enumSetOf(
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
    )

    /**
     * 无来源实体时, 伤害需要考虑物品属性的直接实体类型.
     * 例如: 发射器射出带属性的自定义箭矢.
     */
    private val ATTRIBUTED_ARROW_TYPES: Set<EntityType> = enumSetOf(
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW
    )

    /**
     * 对 [victim] 造成由 [metadata] 指定的萌芽伤害.
     * 当 [damager] 为 `null` 时, 伤害属于无源, 不会产生击退.
     */
    override fun hurt(
        victim: LivingEntity,
        metadata: DamageMetadata,
        damager: LivingEntity?,
        knockback: Boolean,
    ) = RecursionGuard.with(
        functionName = "hurt", silenceLogs = true
    ) {
        victim.registerCustomDamageMetadata(metadata)

        // 如果自定义伤害有源且需要取消击退.
        // 这里修复了: 无源伤害 (即没有造成伤害的 LivingEntity) 不会触发击退事件.
        if (!knockback && damager != null) {
            victim.registerCancelKnockback()
        }

        // 造成伤害
        DamageApplier.INSTANCE.damage(victim, damager, PLACEHOLDER_DAMAGE_VALUE)
    }

    /**
     * 计算“攻击阶段”的伤害信息.
     * 即考虑所有因为伤害发起者导致的影响.
     *
     * @return 攻击阶段的伤害信息, null意为取消伤害事件
     */
    fun createAttackPhaseMetadata(context: DamageContext): DamageMetadata? {
        val damagee = context.damagee
        val customDamageMetadata = damagee.getCustomDamageMetadata()
        if (customDamageMetadata != null) {
            // 该伤害是一个自定义伤害(由代码执行 hurt 造成的),
            // 直接返回已经注册的自定义伤害信息
            damagee.unregisterCustomDamageMetadata() // 注销, 因为已经“用掉”了
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
                if (directEntity.type in ATTRIBUTED_ARROW_TYPES) {
                    // 特殊处理属于 attributed_arrow_types 的 direct_entity, 考虑箭矢本身给予的额外伤害.
                    // 例如: 无源箭矢(发射器)
                    return createNoCausingAttributedArrowDamageMetadata(directEntity as AbstractArrow) ?: context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                } else if (directEntity.type in DIRECT_ATTACKABLE_TYPES) {
                    // 使用映射来计算 direct_entity 造成的伤害.
                    // attributed_arrow_types 中的直接实体类型不会进入此分支
                    // 例如: 无源弹射物(发射器), 无源TNT(发射器), 无源区域效果云(发射器), 无源末影水晶爆炸.
                    return context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                } else {
                    // 不太可能发生
                    return createDefaultDamageMetadata(context)
                }
            }

            is Player -> { // causing_entity 是 player
                if (directEntity.type in REGISTERED_PROJECTILE_TYPES) {
                    // 这里的情况: direct_entity 的 *类型* 在已注册弹射物类型当中.
                    // 这意味着 direct_entity 在之前的某个时间点*也许*已经注册了一个伤害信息.
                    // 先尝试获取已经注册的伤害信息
                    // 若没有注册则考虑弹射物物品上的属性.
                    // 再没有则使用 player_adhoc_mappings.
                    // 例如: 玩家箭矢, 玩家光灵箭, 玩家三叉戟
                    return createRegisteredAbstractArrowDamageMetadata(directEntity as AbstractArrow) ?: context.toDamageMetadata(Mapping.PLAYER_ADHOC)
                } else if (directEntity.type in DIRECT_ATTACKABLE_TYPES) {
                    // 这里的情况: direct_entity 属于游戏里比较特殊的能够造成伤害的实体.
                    // 直接使用 player_adhoc_mappings.
                    // 例如: 玩家其他弹射物, 玩家TNT, 玩家末影水晶
                    return context.toDamageMetadata(Mapping.PLAYER_ADHOC)
                }

                return createPlayerAttackDamageMetadata(context)
            }

            is LivingEntity -> { // causing_entity 是 non-player living_entity
                if (directEntity.type in AS_NULL_CAUSING_DIRECT_ATTACKABLE_TYPES) {
                    // causing_entity 是 living_entity 但 direct_entity
                    // 是 tnt_primed 等特殊情况时, 视为没有 causing_entity
                    // 例如: 非玩家生物点燃的TNT, 非玩家生物引爆的末影水晶
                    return context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                }

                return context.toDamageMetadata(Mapping.ATTACK_CHARACTERISTIC)
            }

            is FallingBlock -> { // causing_entity 是非 living_entity 的下落的方块
                return createFallingBlockDamageMetadata(context)
            }

            else -> { // 不太可能发生, 除非有插件在编造一些不太合法的 DamageSource
                return createDefaultDamageMetadata(context)
            }

        }
    }

    /**
     * 计算“防御阶段”的伤害信息.
     * 即考虑所有因为伤害承受者导致的影响.
     *
     * @return 防御阶段的伤害信息, null意为取消伤害事件
     */
    fun createDefensePhaseMetadata(context: DamageContext): DefenseMetadata? {
        val damagee = context.damagee
        val damageeAttributes = AttributeMapAccess.INSTANCE.get(damagee).getOrElse {
            LOGGER.warn("Failed to generate defense metadata because the entity $damagee does not have an attribute map.")
            return null
        }

        // 受伤者抗性提升等级
        val resistanceLevel = damagee.getPotionEffect(PotionEffectType.RESISTANCE)?.amplifier?.plus(1) ?: 0

        return DefenseMetadata(damageeAttributes, false, resistanceLevel)
    }

    /**
     * 计算 [BASE] 修饰器的伤害.
     * 主要与 Koish 的属性和元素系统有关.
     * 在功能上包含了原版的 [ARMOR] 和 [MAGIC] 修饰器对伤害的影响.
     */
    fun calculateBaseModifierDamage(
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
    ): Double {
        val map = calculateElementDamageMap(damageMetadata, defenseMetadata)
        if (map.isEmpty()) {
            LOGGER.warn("Empty element damage map! There may be some problems with the calculation of damage!", IllegalStateException())
            return .0
        } else {
            return map.values.sum()
        }
    }

    /**
     * 计算 [INVULNERABILITY_REDUCTION] 修饰器的伤害.
     * @return 修饰器的伤害, 正值增加伤害, 负值减少伤害, 空表示不修改
     */
    fun calculateInvulnerabilityModifierDamage(
        /**
         * 在此修饰器处理前, 其他伤害修饰流程处理后的伤害.
         */
        modifiedDamage: Double,
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
    ): Double? {
        // 若此次攻击忽略无懈可击时间则置零
        // 否则不改动
        return if (damageMetadata.ignoreInvulnerability) {
            .0
        } else {
            null
        }
    }

    /**
     * 计算 [BLOCKING] 修饰器的伤害.
     * @return 修饰器的伤害, 正值增加伤害, 负值减少伤害, 空表示不修改
     */
    fun calculateBlockingModifierDamage(
        modifiedDamage: Double,
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
    ): Double? {
        // TODO 根据格挡机制计算
        return .0
    }

    /**
     * 计算 [RESISTANCE] 修饰器的伤害.
     * @return 修饰器的伤害, 正值增加伤害, 负值减少伤害, 空表示不修改
     */
    fun calculateResistanceModifierDamage(
        modifiedDamage: Double,
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
    ): Double? {
        // 若此次攻击忽略抗性提升药水效果则置零
        // 否则根据配置文件指定的抗性提升药水效果伤害减免格式计算
        return if (damageMetadata.ignoreResistance) {
            .0
        } else {
            if (defenseMetadata.resistanceLevel > 0) {
                DamageRules.calculateDamageAfterResistance(modifiedDamage, defenseMetadata.resistanceLevel) - modifiedDamage
            } else {
                .0
            }
        }
    }

    /**
     * 计算 [ABSORPTION] 修饰器的伤害.
     * 实际上红心+黄心的总损失量不变, 只是考虑是否使用黄心抵消伤害
     * @return 修饰器的伤害, 正值增加伤害, 负值减少伤害, 空表示不修改
     */
    fun calculateAbsorptionModifierDamage(
        modifiedDamage: Double,
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
    ): Double? {
        // 若此次攻击忽略伤害吸收则置零
        // 否则不改动
        return if (damageMetadata.ignoreAbsorption) {
            .0
        } else {
            null
        }
    }

    /**
     * 计算各元素的伤害.
     */
    fun calculateElementDamageMap(
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
    ): Reference2DoubleMap<RegistryEntry<Element>> {
        val map = Reference2DoubleOpenHashMap<RegistryEntry<Element>>()
        val damagePackets = damageMetadata.damageBundle.packets()
        damagePackets.forEach { damagePacket ->
            val elementType = damagePacket.element
            val elementDamage = calculateElementDamage(damagePacket, damageMetadata, defenseMetadata)
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
        damagePacket: DamagePacket,
        damageMetadata: DamageMetadata,
        defenseMetadata: DefenseMetadata
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

    private enum class Mapping {
        DAMAGE_TYPE,
        PLAYER_ADHOC,
        NULL_CAUSING_ENTITY,
        ATTACK_CHARACTERISTIC,
    }

    private fun DamageContext.toDamageMetadata(mapping: Mapping): DamageMetadata {
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
    private fun createPlayerAttackDamageMetadata(context: DamageContext): DamageMetadata? {
        val player = context.damageSource.causingEntity as? Player ?: error("The causing entity must be a player.")
        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return PlayerDamageMetadata.INTRINSIC_ATTACK
        val weapon = itemstack.getBehavior<Weapon>() ?: return PlayerDamageMetadata.INTRINSIC_ATTACK
        return weapon.generateDamageMetadata(player, itemstack)
    }

    private fun createFallingBlockDamageMetadata(context: DamageContext): DamageMetadata {
        // TODO
        val fallingBlock = context.damageSource.causingEntity as? FallingBlock ?: error("The causing entity must be a falling block.")
        return VanillaDamageMetadata(context.damage)
    }

    private fun createDefaultDamageMetadata(context: DamageContext): DamageMetadata {
        val damagee = context.damagee
        val directEntity = context.damageSource.directEntity
        val causingEntity = context.damageSource.causingEntity
        LOGGER.warn("Why can ${causingEntity?.type} cause damage to ${damagee.type} through ${directEntity?.type}? This should not happen.")
        return VanillaDamageMetadata(context.damage)
    }

    /**
     * 为 [projectile] 注销伤害.
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
        trident.registerDamage(RegisteredProjectileDamage(1.0 /* TODO 考虑支持修改三叉戟伤害倍率 */, attributes))
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
     * 考虑弹射物物品上的属性.
     * 返回 `null` 表示不由该函数负责.
     */
    private fun createNoCausingAttributedArrowDamageMetadata(arrow: AbstractArrow): DamageMetadata? {
        val itemstack = arrow.itemStack
        if (!itemstack.hasBehaviorExact(ItemBehaviorTypes.ARROW)) return null
        val itemcores = itemstack.getData(ItemDataTypes.CORE_CONTAINER) ?: return null
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
     * 先尝试获取已经注册的伤害信息
     * 若没有注册则考虑弹射物物品上的属性.
     * 返回 `null` 表示不由该函数负责.
     */
    private fun createRegisteredAbstractArrowDamageMetadata(abstractArrow: AbstractArrow): DamageMetadata? {
        val registeredProjectileDamage = abstractArrow.getRegisteredDamage()
        val attributes = registeredProjectileDamage?.attributes ?: getImaginaryArrowAttributes() ?: return null
        val force = registeredProjectileDamage?.force ?: 1.0
        val damageBundle = run {
            val itemstack = abstractArrow.itemStack
            if (!itemstack.hasBehaviorExact(ItemBehaviorTypes.ARROW)) return null
            val itemcores = itemstack.getData(ItemDataTypes.CORE_CONTAINER) ?: return null
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
     * 包含已注册的 [DamageMetadata], 用于让代码对实体造成任意的自定义伤害(减免前).
     *
     * 不要跟 [registeredProjectileDamages] 搞混了, 这里的伤害基本来源于由代码额外造成的伤害(比如: 武器攻击特效, MythicMobs Mechanic).
     */
    private val registeredCustomDamageMetadata = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .build<UUID, DamageMetadata>()

    private fun Entity.getCustomDamageMetadata(): DamageMetadata? {
        return registeredCustomDamageMetadata.getIfPresent(uniqueId)
    }

    private fun Entity.registerCustomDamageMetadata(attributes: DamageMetadata) {
        registeredCustomDamageMetadata.put(uniqueId, attributes)
    }

    private fun Entity.unregisterCustomDamageMetadata() {
        registeredCustomDamageMetadata.invalidate(uniqueId)
    }

    /**
     * 包含已注册的 [Projectile] 的 [cc.mewcraft.wakame.entity.attribute.AttributeMapSnapshot], 用于在弹射物刚被创建时就固定其伤害(减免前).
     *
     * 不要跟 [registeredCustomDamageMetadata] 搞混了, 这里的属性快照仅仅用于实现弹射物的伤害计算.
     * 这样设计可以让弹射物的属性在*打中*实体时被拦截和修改, 允许代码根据受伤实体的状态来修改属性;
     * 而不是*创建*弹射物时拦截属性和修改 - 代码无法从这个时机得知受伤实体的状态.
     */
    private val registeredProjectileDamages: Cache<UUID, RegisteredProjectileDamage> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .build()

    private fun Projectile.getRegisteredDamage(): RegisteredProjectileDamage? {
        return registeredProjectileDamages.getIfPresent(uniqueId)
    }

    private fun Projectile.registerDamage(damage: RegisteredProjectileDamage) {
        registeredProjectileDamages.put(uniqueId, damage)
    }

    private fun Projectile.unregisterDamage() {
        registeredProjectileDamages.invalidate(uniqueId)
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
}
