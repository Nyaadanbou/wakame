@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.attack.SwordAttack
import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.damage.DamageManager.calculateDamageBeforeDefense
import cc.mewcraft.wakame.damage.DamageManager.calculateFinalDamage
import cc.mewcraft.wakame.damage.DamageManager.calculateFinalDamageMap
import cc.mewcraft.wakame.damage.DamageManager.registerExactArrow
import cc.mewcraft.wakame.damage.DamageManager.registerTrident
import cc.mewcraft.wakame.damage.DamageManager.registeredCustomDamageMetadata
import cc.mewcraft.wakame.damage.DamageManager.registeredProjectileDamages
import cc.mewcraft.wakame.damage.mapping.AttackCharacteristicDamageMappings
import cc.mewcraft.wakame.damage.mapping.DamageTypeDamageMappings
import cc.mewcraft.wakame.damage.mapping.NullCausingDamageMappings
import cc.mewcraft.wakame.damage.mapping.PlayerAdhocDamageMappings
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.event.bukkit.NekoPreprocessDamageEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.user.attributeContainer
import cc.mewcraft.wakame.util.RecursionGuard
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import org.bukkit.Material
import org.bukkit.damage.DamageSource
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.jetbrains.annotations.ApiStatus
import xyz.xenondevs.commons.collections.enumSetOf
import java.time.Duration
import java.util.*
import kotlin.math.round

/**
 * @see DamageManagerApi.hurt
 */
fun LivingEntity.hurt(damageMetadata: DamageMetadata, source: LivingEntity? = null, knockback: Boolean = false) {
    DamageManager.hurt(this, damageMetadata, source, knockback)
}

interface DamageManagerApi {

    /**
     * 对 [victim] 造成由 [metadata] 指定的自定义伤害.
     *
     * 当 [damager] 为 `null` 时, 伤害属于无源, 不会产生击退效果.
     *
     * @param victim 受到伤害的实体
     * @param metadata 伤害的元数据
     * @param damager 造成伤害的实体
     * @param knockback 是否产生击退效果
     */
    fun hurt(
        victim: LivingEntity,
        metadata: DamageMetadata,
        damager: LivingEntity? = null,
        knockback: Boolean = false,
    )

    /**
     * 伴生对象, 提供 [DamageManagerApi] 的实例.
     */
    companion object {

        @get:JvmName("getInstance")
        lateinit var INSTANCE: DamageManagerApi
            private set

        @ApiStatus.Internal
        fun register(instance: DamageManagerApi) {
            this.INSTANCE = instance
        }

    }

}

// ------------
// 内部实现
// ------------

internal fun DamageContext(event: EntityDamageEvent): DamageContext {
    val damage = event.damage
    val damagee = event.entity as? LivingEntity ?: error("The damagee must be a living entity")
    val damageSource = event.damageSource
    val damageCause = event.cause
    return DamageContext(damage, damagee, damageSource, damageCause)
}

internal class DamageContext(
    val damage: Double,
    val damagee: LivingEntity,
    val damageSource: DamageSource,
    val damageCause: DamageCause,
) {
    override fun toString(): String {
        return "DamageContext(damage=$damage, damagee=$damagee, damageCause=$damageCause, damageType=${damageSource.damageType}, causingEntity=${damageSource.causingEntity}, directEntity=${damageSource.directEntity}, damageLocation=${damageSource.damageLocation})"
    }
}

/**
 * 包含伤害系统的核心逻辑和状态.
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
        EntityType.POTION,
        EntityType.SHULKER_BULLET,
        EntityType.SMALL_FIREBALL,
        EntityType.SNOWBALL,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
        EntityType.WIND_CHARGE,
    )
    /**
     * 作为 direct_entity 时能够造成伤害的 entity 类型.
     */
    private val MISC_DAMAGER_TYPES: Set<EntityType> = PROJECTILE_DAMAGER_TYPES + enumSetOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
        EntityType.AREA_EFFECT_CLOUD,
    )

    /**
     * 作为 direct_entity 时能够造成伤害的并且比较“特殊”的 entity 类型.
     */
    private val SPECIAL_DAMAGER_TYPES: Set<EntityType> = enumSetOf(
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
     * 可以作为(用弓/弩射出的)“箭矢”的实体类型.
     */
    // 如果 BukkitAPI 存在一个这样的接口表示这类实体,
    // 那我们也就不需要这么一个 set 了, 可以直接用 is.
    // 问题就是没有, 所以只能这样了.
    private val EXACT_ARROW_TYPES: Set<EntityType> = enumSetOf(
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
     * 计算“攻击阶段”之后-“防御阶段”之前-的伤害.
     *
     * @return 攻击阶段的伤害信息
     * @see calculateFinalDamage 计算“防御阶段”之后的单个元素的伤害
     * @see calculateFinalDamageMap 计算“防御阶段”之后的所有元素的伤害之和
     */
    fun calculateDamageBeforeDefense(context: DamageContext): DamageMetadata? {
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
                if (directEntity.type in EXACT_ARROW_TYPES) {
                    // 特殊处理属于 exact_arrow_types 的 direct_entity, 考虑箭矢本身给予的额外伤害.
                    return createAttributedArrowDamage(directEntity as AbstractArrow)
                } else if (directEntity.type in MISC_DAMAGER_TYPES) {
                    // 使用映射来计算 direct_entity 造成的伤害.
                    return context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                } else {
                    // 不太可能发生
                    return calculateDefaultDamageMetadata(context)
                }
            }

            is Player -> {
                if (directEntity.type in REGISTERED_PROJECTILE_TYPES) {
                    // 这里的情况: direct_entity 的 *类型* 在已注册弹射物类型当中.
                    // 这意味着 direct_entity 在之前的某个时间点*也许*已经注册了一个伤害信息.
                    // 因此这里先尝试获取已经注册的伤害信息; 若没有注册, 则使用 player_adhoc_mappings.
                    return calculateAbstractArrowDamage(causingEntity, directEntity as AbstractArrow, context) ?: context.toDamageMetadata(Mapping.PLAYER_ADHOC)
                } else if (directEntity.type in MISC_DAMAGER_TYPES) {
                    // 这里的情况: direct_entity 属于游戏里比较特殊的能够造成伤害的实体.
                    // 直接使用 player_adhoc_mappings.
                    return context.toDamageMetadata(Mapping.PLAYER_ADHOC)
                }

                when (context.damageCause) {
                    DamageCause.ENTITY_ATTACK -> { // 左键直接点到了实体
                        return createPlayerDirectAttackDamageMetadata(context)
                    }

                    DamageCause.ENTITY_SWEEP_ATTACK -> { // 横扫之刃“溅射”到了实体
                        return createPlayerSweepAttackDamageMetadata(context)
                    }

                    else -> { // 不太可能
                        return calculateDefaultDamageMetadata(context)
                    }
                }
            }

            is LivingEntity -> {

                if (directEntity.type in SPECIAL_DAMAGER_TYPES) {
                    // 特殊处理: causing_entity 是 living_entity 但 direct_entity
                    // 是 tnt_primed 或 ender_crystal 时, 视为没有 causing_entity
                    return context.toDamageMetadata(Mapping.NULL_CAUSING_ENTITY)
                }

                return context.toDamageMetadata(Mapping.ATTACK_CHARACTERISTIC)
            }

            else -> { // 不可能发生
                return calculateDefaultDamageMetadata(context)
            }

        }
    }

    /**
     * @see calculateFinalDamageMap
     */
    fun calculateFinalDamageMap(
        damageMetadata: DamageMetadata,
        damagee: LivingEntity,
    ): Reference2DoubleMap<RegistryEntry<ElementType>> {
        val damageeAttributes = AttributeMapAccess.INSTANCE.get(damagee).getOrElse {
            error("Failed to generate defense metadata because the entity $damagee does not have an attribute map.")
        }
        return calculateFinalDamageMap(damageMetadata, damageeAttributes)
    }

    /**
     * 计算所有元素-“防御阶段”之后-的伤害之和, 即最终伤害.
     *
     * @param damageMetadata “攻击阶段”的信息, 该信息通过 [calculateDamageBeforeDefense] 计算得出
     * @param damageeAttributes 受伤实体的属性
     */
    private fun calculateFinalDamageMap(
        damageMetadata: DamageMetadata,
        damageeAttributes: AttributeMap,
    ): Reference2DoubleMap<RegistryEntry<ElementType>> {
        val damagePackets = damageMetadata.damageBundle.packets()
        if (damagePackets.isEmpty()) {
            // 记录空伤害包以方便定位问题
            LOGGER.warn("Empty damage bundle!", IllegalStateException())
            return Reference2DoubleMaps.emptyMap()
        } else {
            val res = Reference2DoubleOpenHashMap<RegistryEntry<ElementType>>()
            damagePackets.forEach { damagePacket ->
                val elementType = damagePacket.element
                val criticalStrikeMetadata = damageMetadata.criticalStrikeMetadata
                val damage = calculateFinalDamage(damagePacket, criticalStrikeMetadata, damageeAttributes)
                if (damage > 0) {
                    res[elementType] = damage
                }
            }
            return res
        }
    }

    /**
     * 计算单个元素-“防御阶段”之后-的伤害.
     *
     * @param damagePacket “攻击阶段”的伤害信息
     * @param criticalStrikeMetadata 暴击信息
     * @param damageeAttributes 受伤实体的属性
     */
    private fun calculateFinalDamage(
        damagePacket: DamagePacket,
        criticalStrikeMetadata: CriticalStrikeMetadata,
        damageeAttributes: AttributeMap,
    ): Double {
        // 伤害包的元素类型
        val elementType = damagePacket.element
        // 该元素伤害倍率(或称攻击威力)
        val attackDamageRate = damagePacket.rate
        // 暴击倍率
        val criticalStrikePower = criticalStrikeMetadata.power
        // 受伤者防御, 不会小于0
        val defense = (damageeAttributes.getValue(Attributes.DEFENSE.of(elementType)) + damageeAttributes.getValue(Attributes.UNIVERSAL_DEFENSE)).coerceAtLeast(0.0)
        // 受伤者承伤倍率
        val incomingDamageRate = damageeAttributes.getValue(Attributes.INCOMING_DAMAGE_RATE.of(elementType))

        // 计算原始伤害
        var originalDamage = damagePacket.packetDamage
        if (DamageRules.ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE) {
            originalDamage *= attackDamageRate
        }
        if (DamageRules.CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE) {
            originalDamage *= criticalStrikePower
        }

        // 计算有效防御
        val validDefense = DamageRules.calculateValidDefense(
            defense = defense,
            defensePenetration = damagePacket.defensePenetration,
            defensePenetrationRate = damagePacket.defensePenetrationRate
        )

        // 计算防御后伤害
        val damageAfterDefense = DamageRules.calculateDamageAfterDefense(
            originalDamage = originalDamage,
            validDefense = validDefense
        )

        // 计算最终伤害
        var finalDamage = damageAfterDefense * incomingDamageRate
        if (!DamageRules.ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE) {
            finalDamage *= attackDamageRate
        }
        if (!DamageRules.CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE) {
            finalDamage *= criticalStrikePower
        }
        val leastDamage = if (damagePacket.packetDamage > 0) DamageRules.LEAST_DAMAGE else 0.0
        finalDamage = finalDamage.coerceAtLeast(leastDamage)

        if (DamageRules.ROUNDING_DAMAGE) {
            finalDamage = round(finalDamage)
        }

        return finalDamage
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

    private fun createAttributedArrowDamage(arrow: AbstractArrow): DamageMetadata? {
        val itemstack = arrow.itemStack.wrap() ?: return null
        if (!itemstack.templates.has(ItemTemplateTypes.ARROW)) return null
        val itemcells = itemstack.components.get(ItemComponentTypes.CELLS) ?: return null
        val modifiersOnArrow = itemcells.collectAttributeModifiers(itemstack, ItemSlot.imaginary())
        val arrowAttributes = ImaginaryAttributeMaps.ARROW.value.getSnapshot()
        arrowAttributes.addTransientModifiers(modifiersOnArrow)
        val damageBundle = damageBundle(arrowAttributes) {
            every {
                standard()
            }
        }
        return VanillaDamageMetadata(damageBundle)
    }

    // 可以返回 null, 意为取消本次伤害
    private fun createPlayerDirectAttackDamageMetadata(context: DamageContext): DamageMetadata? {
        val player = context.damageSource.causingEntity as? Player ?: error("The causing entity must be a player.")
        val itemstack = player.inventory.itemInMainHand.wrap() ?: error("No koish item in player main hand.")
        val attack = itemstack.templates.get(ItemTemplateTypes.ATTACK) ?: return PlayerDamageMetadata.INTRINSIC_ATTACK
        return attack.attackType.generateDamageMetadata(player, itemstack)
    }

    // 可以返回 null, 意为取消本次伤害
    private fun createPlayerSweepAttackDamageMetadata(context: DamageContext): DamageMetadata? {
        // TODO 横扫的临时实现, 等待麻将的组件化
        // 需要玩家手中的物品是 Attack 且攻击特效是 “sword” 才能打出横扫伤害
        val player = context.damageSource.causingEntity as? Player ?: error("The causing entity must be a player.")
        val itemstack = player.inventory.itemInMainHand.wrap() ?: error("No koish item in player main hand.")
        val attack = itemstack.templates.get(ItemTemplateTypes.ATTACK) ?: return null
        if (attack.attackType !is SwordAttack) return null // 返回 null 是为了供外部识别以不触发萌芽伤害事件
        val playerAttributes = player.attributeContainer
        val damageBundle = damageBundle(playerAttributes) {
            every {
                standard()
                rate { standard() * playerAttributes.getValue(Attributes.SWEEPING_DAMAGE_RATIO) }
            }
        }
        return PlayerDamageMetadata(
            attributes = playerAttributes,
            damageBundle = damageBundle
        )
    }

    private fun calculateDefaultDamageMetadata(context: DamageContext): DamageMetadata {
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
     * 返回 `null` 表示不由该函数负责.
     */
    private fun calculateAbstractArrowDamage(causingEntity: Player, abstractArrow: AbstractArrow, context: DamageContext): DamageMetadata? {
        val (force, attributes) = abstractArrow.getRegisteredDamage() ?: return null

        val event = NekoPreprocessDamageEvent.actuallyDamage(causingEntity, attributes, context).apply { callEvent() }

        val attributes2 = event.causingAttributes // 拦截/修改后的属性快照
        val damageBundle = run {
            val itemstack = abstractArrow.itemStack.wrap()
            val itemcells = itemstack?.components?.get(ItemComponentTypes.CELLS)
            if (itemstack?.templates?.has(ItemTemplateTypes.ARROW) == true && itemcells != null) {
                // As of 1.21.3, AbstractArrow = 箭矢/光灵箭矢/三叉戟,
                // 这些物品上在我们的游戏设计上可能有修改伤害的属性.
                // 该分支将考虑这些属性.

                val modifiersOnArrow = itemcells.collectAttributeModifiers(itemstack, ItemSlot.imaginary())
                attributes2.addTransientModifiers(modifiersOnArrow)
                damageBundle(attributes2) {
                    every {
                        standard()
                        min { force * standard() }
                        max { force * standard() }
                    }
                }
            } else {
                damageBundle(attributes2) {
                    every {
                        standard()
                        min { force * standard() }
                        max { force * standard() }
                    }
                }
            }
        }
        return PlayerDamageMetadata(
            attributes = attributes2,
            damageBundle = damageBundle
        )
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
     * 包含已注册的 [Projectile] 的 [AttributeMapSnapshot], 用于在弹射物刚被创建时就固定其伤害(减免前).
     *
     * 不要跟 [registeredCustomDamageMetadata] 搞混了, 这里的属性快照仅仅用于实现弹射物的伤害计算.
     * 这样设计可以让弹射物的属性在*打中*实体时被拦截和修改, 允许代码根据受伤实体的状态来修改属性;
     * 而不是*创建*弹射物时拦截属性和修改 - 代码无法从这个时机得知受伤实体的状态.
     */
    private val registeredProjectileDamages: Cache<UUID, RegisteredProjectileDamage> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(10))
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

    // TODO 更通用的临时标记工具类
}
