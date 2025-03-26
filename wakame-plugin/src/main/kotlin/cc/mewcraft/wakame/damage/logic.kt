@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.attack.SwordAttack
import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ImaginaryAttributeMaps
import cc.mewcraft.wakame.damage.DamageManager.calculateFinalDamage
import cc.mewcraft.wakame.damage.DamageManager.calculateFinalDamageMap
import cc.mewcraft.wakame.damage.mapping.AttackCharacteristicDamageMappings
import cc.mewcraft.wakame.damage.mapping.DamageTypeDamageMappings
import cc.mewcraft.wakame.damage.mapping.DirectEntityDamageMappings
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.RecursionGuard
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.jetbrains.annotations.ApiStatus
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

/**
 * 包含伤害系统的核心逻辑和状态.
 */
internal object DamageManager : DamageManagerApi {

    // 特殊值, 方便识别. 仅用于触发事件, 以被事件系统监听&修改.
    private const val PLACEHOLDER_DAMAGE_VALUE = 4.95

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
     * 能够造成伤害的弹射物类型.
     * 伤害系统会处理这些弹射物.
     */
    private val DAMAGE_PROJECTILE_TYPES: List<EntityType> = listOf(
        EntityType.ARROW,
        EntityType.BREEZE_WIND_CHARGE,
        //EntityType.DRAGON_FIREBALL,
        EntityType.EGG,
        EntityType.ENDER_PEARL,
        EntityType.FIREBALL,
        EntityType.FIREWORK_ROCKET,
        //EntityType.FISHING_BOBBER,
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
     * 作为直接实体时, 能够造成伤害的实体类型.
     * 伤害系统会处理这些实体.
     */
    private val DAMAGE_DIRECT_ENTITY_TYPES = DAMAGE_PROJECTILE_TYPES + listOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
        EntityType.AREA_EFFECT_CLOUD,
    )

    /**
     * 需要记录的弹射物.
     * 伤害系统会在这些弹射物生成时就记录伤害元数据.
     */
    private val MARK_PROJECTILE_TYPES: List<EntityType> = listOf(
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
    )

    /**
     * 计算“攻击阶段”之后的伤害. 返回的 [DamageMetadata] 包含了攻击阶段的伤害信息.
     *
     * @see calculateFinalDamage 计算“防御阶段”之后的单个元素的伤害
     * @see calculateFinalDamageMap 计算“防御阶段”之后的所有元素的伤害之和
     */
    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata? {
        // 先检查是不是自定义伤害; 若是, 则直接返回自定义伤害信息
        val customDamageMetadata = event.entity.getCustomDamageMetadata()
        if (customDamageMetadata != null) {
            event.entity.unregisterCustomDamageMetadata() // FIXME #366: x
            return customDamageMetadata
        }

        val damageSource = event.damageSource
        val causingEntity = damageSource.causingEntity
        val directEntity = damageSource.directEntity

        // 不存在直接实体
        // 自然伤害(溺水、岩浆)
        // 使用伤害类型映射, 根据伤害类型调整伤害
        if (directEntity == null) {
            val damageMapper = DamageTypeDamageMappings.get(damageSource.damageType)
            return damageMapper.generate(event)
        }

        // 存在直接实体后
        when (causingEntity) {
            // 无来源实体
            null -> {
                if (directEntity.type in DAMAGE_DIRECT_ENTITY_TYPES) {
                    // 使用默认
                    return buildDirectEntityDamageMetadataByDefault(directEntity, event, false)
                } else {
                    // 不应该发生
                    return generateDefaultMetadata(event)
                }
            }
            // 来源实体是玩家
            is Player -> {
                // 直接实体是需要记录弹射物
                // 查找弹射物是否被记录, 尝试返回记录的值
                // 箭矢和三叉戟的伤害和词条栏有关, 会被记录
                // 没有记录则使用默认
                if (directEntity.type in MARK_PROJECTILE_TYPES) {
                    return (directEntity as Projectile).getProjectileDamageMetadata() ?: buildDirectEntityDamageMetadataByDefault(directEntity, event, true)  // FIXME #366: x
                } else if (directEntity.type in DAMAGE_DIRECT_ENTITY_TYPES) {
                    return buildDirectEntityDamageMetadataByDefault(directEntity, event, true)
                }

                val itemStack = causingEntity.inventory.itemInMainHand
                val nekoStack = itemStack.wrap()
                val attack = nekoStack?.templates?.get(ItemTemplateTypes.ATTACK)
                when (event.cause) {
                    DamageCause.ENTITY_ATTACK -> {
                        if (attack != null) {
                            // 玩家手中的物品是 Attack
                            // 可能返回null, 意为取消伤害事件
                            return attack.attackType.generateDamageMetadata(causingEntity, nekoStack) // FIXME #366: v
                        } else {
                            // 手中的物品无 Attack 行为甚至不是 NekoStack
                            return PlayerDamageMetadata.HAND_WITHOUT_ATTACK // FIXME #366: x
                        }
                    }

                    DamageCause.ENTITY_SWEEP_ATTACK -> {
                        // TODO 横扫的临时实现, 等待麻将的组件化
                        // 需要玩家手中的物品是 Attack 且攻击特效是 “sword” 才能打出横扫伤害
                        if (attack?.attackType is SwordAttack) {
                            val attributeMap = causingEntity.toUser().attributeMap // FIXME #366: v
                            return PlayerDamageMetadata(
                                user = causingEntity.toUser(),
                                damageTags = DamageTags(DamageTag.MELEE, DamageTag.SWORD),
                                damageBundle = damageBundle(attributeMap) {
                                    every {
                                        standard()
                                        rate { standard() * attributeMap.getValue(Attributes.SWEEPING_DAMAGE_RATIO) }
                                    }
                                }
                            )
                        } else {
                            // 返回 null 是为了供外部识别以不触发萌芽伤害事件
                            return null
                        }
                    }

                    else -> {
                        // 其他 Cause 的玩家伤害
                        // 不应该发生
                        return generateDefaultMetadata(event)
                    }
                }
            }
            // 来源实体是非玩家生物
            is LivingEntity -> {
                // 直接实体是TNT、末影水晶, 视为无源处理
                if (directEntity.type == EntityType.TNT || directEntity.type == EntityType.END_CRYSTAL) {
                    return buildDirectEntityDamageMetadataByDefault(directEntity, event, false)
                }
                val damageMapper = AttackCharacteristicDamageMappings.get(causingEntity, event)
                if (damageMapper == null) {
                    // 配置文件未指定该情景下生物的伤害映射
                    // 返回默认元素、无防御穿透、无暴击、原版伤害值
                    LOGGER.warn("The vanilla entity damage from '${damageSource.causingEntity?.type}' to '${event.entity.type}' by '${damageSource.directEntity?.type}' with damage type of '${damageSource.damageType.key}' is not config! Use default damage metadata.")
                    return EntityDamageMetadata(
                        damageBundle = damageBundle {
                            default {
                                min(event.damage)
                                max(event.damage)
                                rate(1.0)
                                defensePenetration(0.0)
                                defensePenetrationRate(0.0)
                            }
                        },
                        criticalStrikeMetadata = CriticalStrikeMetadata.NONE
                    )
                } else {
                    // 配置文件指定了该情景下生物的伤害映射
                    return damageMapper.generate(event)
                }
            }
            // 不可能发生
            else -> {
                return generateDefaultMetadata(event)
            }
        }
    }

    /**
     * 计算单个元素“防御阶段”之后的伤害.
     */
    private fun calculateFinalDamage(
        elementType: RegistryEntry<ElementType>,
        damageMetadata: DamageMetadata,
        damageeAttributes: AttributeMap,
    ): Double {
        // 当该元素的伤害包不存在时, 返回 0.0
        val packet = damageMetadata.damageBundle.get(elementType) ?: return 0.0

        // 该元素伤害倍率(或称攻击威力)
        val attackDamageRate = packet.rate
        // 暴击倍率
        val criticalStrikePower = damageMetadata.criticalStrikeMetadata.power
        // 受伤者防御, 不会小于0
        val defense = (damageeAttributes.getValue(Attributes.DEFENSE.of(elementType)) + damageeAttributes.getValue(Attributes.UNIVERSAL_DEFENSE)).coerceAtLeast(0.0)
        // 受伤者承伤倍率
        val incomingDamageRate = damageeAttributes.getValue(Attributes.INCOMING_DAMAGE_RATE.of(elementType))

        // 计算原始伤害
        var originalDamage = packet.packetDamage
        if (DamageRules.ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE) {
            originalDamage *= attackDamageRate
        }
        if (DamageRules.CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE) {
            originalDamage *= criticalStrikePower
        }

        // 计算有效防御
        val validDefense = DamageRules.calculateValidDefense(
            defense = defense,
            defensePenetration = packet.defensePenetration,
            defensePenetrationRate = packet.defensePenetrationRate
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
        val leastDamage = if (packet.packetDamage > 0) DamageRules.LEAST_DAMAGE else 0.0
        finalDamage = finalDamage.coerceAtLeast(leastDamage)

        if (DamageRules.ROUNDING_DAMAGE) {
            finalDamage = round(finalDamage)
        }

        return finalDamage
    }

    /**
     * 计算所有元素“防御阶段”之后的伤害之和, 即最终伤害.
     *
     * @param damageMetadata “攻击阶段”的信息
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
            damagePackets.forEach { packet ->
                val element = packet.element
                val damage = calculateFinalDamage(element, damageMetadata, damageeAttributes)
                if (damage > 0) {
                    res[element] = damage
                }
            }
            return res
        }
    }

    private val ARROW_TYPES: List<EntityType> = listOf(EntityType.ARROW, EntityType.SPECTRAL_ARROW)

    /**
     * 构建无来源实体直接伤害实体的伤害元数据.
     * 使用直接伤害实体类型映射, 根据弹射物类型获取伤害元数据.
     * 箭矢会被特殊处理, 考虑词条栏给予的额外伤害.
     */
    private fun buildDirectEntityDamageMetadataByDefault(directEntity: Entity, event: EntityDamageEvent, forPlayer: Boolean): DamageMetadata {
        val entityType = directEntity.type
        if (entityType in ARROW_TYPES) {
            val damageBundle = buildArrowDamageBundleByCells(directEntity as AbstractArrow)
            if (damageBundle != null) {
                return VanillaDamageMetadata(damageBundle)
            }
        }
        val damageMapper = if (forPlayer) {
            DirectEntityDamageMappings.getForPlayer(entityType, event)
        } else {
            DirectEntityDamageMappings.getForUntracked(entityType, event)
        }
        if (damageMapper == null) {
            LOGGER.warn("The damage from 'null' to '${event.entity.type}' by '${entityType}' with damage type of '${event.damageSource.damageType.key}' is not defined in the config! Fallback to default damage metadata.")
            return VanillaDamageMetadata(event.damage)
        }
        return damageMapper.generate(event)
    }

    private fun buildArrowDamageBundleByCells(arrow: AbstractArrow): DamageBundle? {
        val itemStack = arrow.itemStack
        val nekoStack = itemStack.wrap() ?: return null
        if (!nekoStack.templates.has(ItemTemplateTypes.ARROW)) return null
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return null
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
        val arrowImaginaryAttributeMap by ImaginaryAttributeMaps.ARROW
        val attributeMapSnapshot = arrowImaginaryAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }
        return damageBundle(attributeMapSnapshot) {
            every {
                standard()
            }
        }
    }

    private fun generateDefaultMetadata(event: EntityDamageEvent): DamageMetadata {
        val damageSource = event.damageSource
        LOGGER.warn("Why can ${damageSource.causingEntity?.type} cause damage to ${event.entity.type} through ${damageSource.directEntity?.type}? This should not happen.")
        return VanillaDamageMetadata(event.damage)
    }

    fun generateDefenseMetadata(event: EntityDamageEvent): DefenseMetadata {
        return when (val damagee = event.entity) {
            is Player -> {
                EntityDefenseMetadata(damagee.toUser().attributeMap)
            }

            is LivingEntity -> {
                EntityDefenseMetadata(AttributeMapAccess.instance().get(damagee).getOrElse {
                    error("Failed to generate defense metadata because the entity does not have an attribute map.")
                })
            }

            else -> {
                throw IllegalArgumentException("Damagee is not a living entity!")
            }
        }
    }

    private val projectileDamageMetadataMap = Caffeine.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofSeconds(60))
        .build<UUID, DamageMetadata>()

    fun unregisterProjectile(projectile: Projectile) {
        projectileDamageMetadataMap.invalidate(projectile.uniqueId)
    }

    private fun Projectile.registerProjectileDamageMetadata(damageMetadata: DamageMetadata) {
        projectileDamageMetadataMap.put(uniqueId, damageMetadata)
    }

    private fun Projectile.getProjectileDamageMetadata(): DamageMetadata? {
        return projectileDamageMetadataMap.getIfPresent(uniqueId)
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata].
     * 目前只记录玩家的箭矢和三叉戟.
     *
     * 伤害元数据过期的情况如下 (满足其一):
     * - 超过有效期 (60秒)
     * - 击中方块
     */
    fun registerProjectile(event: ProjectileLaunchEvent) {
        val projectile = event.entity

        // FIXME #366: v

        val shooter = projectile.shooter
        if (shooter !is Player) return
        when (projectile) {
            is Trident -> {
                val key = projectile.uniqueId
                val metadata = PlayerDamageMetadata(
                    user = shooter.toUser(),
                    damageTags = DamageTags(DamageTag.PROJECTILE, DamageTag.TRIDENT),
                    damageBundle = damageBundle(shooter.toUser().attributeMap) {
                        every {
                            standard()
                        }
                    }
                )
                projectileDamageMetadataMap.put(key, metadata)
            }

            is Arrow, is SpectralArrow -> {
                // 玩家箭矢的伤害信息需要拉弓力度
                // 由 #registerProjectile(EntityShootBowEvent) 处理
            }
        }
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata]. 目前只记录玩家的箭矢和三叉戟.
     * 玩家射出的箭矢伤害需要根据拉弓的力度进行调整, 所以监听此事件而非 [ProjectileLaunchEvent].
     *
     * 伤害元数据过期的情况如下 (满足其一):
     * - 超过有效期 (60秒)
     * - 击中方块
     */
    fun registerProjectile(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity !is Player) return

        // FIXME #366: v

        val projectile = event.projectile
        if (projectile !is AbstractArrow) return

        val force: Double
        val damageTags: DamageTags
        val damageBundle: DamageBundle
        val bowMaterial = event.bow?.type
        when (bowMaterial) {
            Material.BOW -> {
                damageTags = DamageTags(DamageTag.PROJECTILE, DamageTag.BOW)
                // 玩家射出的箭矢伤害需要根据拉弓的力度进行调整
                force = DamageRules.calculateBowForce(entity.activeItemUsedTime)
            }

            Material.CROSSBOW -> {
                damageTags = DamageTags(DamageTag.PROJECTILE, DamageTag.CROSSBOW)
                force = 1.0
            }

            else -> {
                return
            }
        }

        val itemstack = projectile.itemStack.wrap()
        val cells = itemstack?.components?.get(ItemComponentTypes.CELLS)
        if (itemstack?.templates?.has(ItemTemplateTypes.ARROW) == true && cells != null) {
            val attributeModifiers = cells.collectAttributeModifiers(itemstack, ItemSlot.imaginary())
            val attributeMapSnapshot = entity.toUser().attributeMap.getSnapshot()
            attributeModifiers.forEach { attribute, modifier ->
                attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
            }
            damageBundle = damageBundle(attributeMapSnapshot) {
                every {
                    standard()
                    min { force * standard() }
                    max { force * standard() }
                }
            }
        } else {
            damageBundle = damageBundle(entity.toUser().attributeMap) {
                every {
                    standard()
                    min { force * standard() }
                    max { force * standard() }
                }
            }
        }

        projectile.registerProjectileDamageMetadata(
            PlayerDamageMetadata(
                user = entity.toUser(),
                damageTags = damageTags,
                damageBundle = damageBundle
            )
        )
    }

    private val customDamageMetadataMap: Cache<UUID, DamageMetadata> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(4))
        .build()

    private fun Entity.getCustomDamageMetadata(): DamageMetadata? {
        return customDamageMetadataMap.getIfPresent(uniqueId)
    }

    private fun Entity.registerCustomDamageMetadata(metadata: DamageMetadata) {
        customDamageMetadataMap.put(uniqueId, metadata)
    }

    private fun Entity.unregisterCustomDamageMetadata() {
        customDamageMetadataMap.invalidate(uniqueId)
    }

    private val cancelKnockbackSet: HashSet<UUID> = HashSet()

    private fun Entity.registerCancelKnockback() {
        cancelKnockbackSet.add(uniqueId)
    }

    fun unregisterCancelKnockback(entity: Entity): Boolean {
        return cancelKnockbackSet.remove(entity.uniqueId)
    }

    // TODO 更通用的临时标记工具类
}
