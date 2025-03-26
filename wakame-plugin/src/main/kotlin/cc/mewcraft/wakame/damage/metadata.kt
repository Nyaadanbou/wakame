@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.molang.Expression
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.require
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.SerializationException
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type
import kotlin.math.absoluteValue
import kotlin.math.round
import kotlin.random.Random
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 防御元数据.
 * 包含了一次伤害中“防御阶段”的有关信息.
 * 在实例化后, 最终伤害数值以及各种信息就已经确定了.
 */
sealed interface DefenseMetadata {
    val damageeAttributeMap: AttributeMap

    /**
     * 计算各元素最终伤害的方法.
     */
    fun calculateFinalDamage(element: RegistryEntry<ElementType>, damageMetadata: DamageMetadata): Double
}

/**
 * 玩家和非玩家实体的防御元数据.
 */
internal class EntityDefenseMetadata(
    override val damageeAttributeMap: AttributeMap,
) : DefenseMetadata {
    override fun calculateFinalDamage(element: RegistryEntry<ElementType>, damageMetadata: DamageMetadata): Double {
        // 当该元素的伤害包不存在时, 返回 0.0
        val packet = damageMetadata.damageBundle.get(element) ?: return 0.0

        // 该元素伤害倍率(或称攻击威力)
        val attackDamageRate = packet.rate
        // 暴击倍率
        val criticalStrikePower = damageMetadata.criticalStrikeMetadata.power
        // 受伤者防御, 不会小于0
        val defense = (damageeAttributeMap.getValue(Attributes.DEFENSE.of(element)) + damageeAttributeMap.getValue(Attributes.UNIVERSAL_DEFENSE)).coerceAtLeast(0.0)
        // 受伤者承伤倍率
        val incomingDamageRate = damageeAttributeMap.getValue(Attributes.INCOMING_DAMAGE_RATE.of(element))

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

        if (DamageRules.ROUNDING_DAMAGE){
            finalDamage = round(finalDamage)
        }

        return finalDamage
    }
}

/**
 * 伤害元数据, 包含了一次伤害中"攻击阶段"的有关信息.
 * 一旦实例化后, 攻击伤害的数值以及各种信息就已经确定.
 */
data class DamageMetadata(
    /**
     * 伤害标签.
     */
    val damageTags: DamageTags,

    /**
     * 伤害捆绑包.
     */
    val damageBundle: DamageBundle,

    /**
     * 暴击元数据.
     */
    val criticalStrikeMetadata: CriticalStrikeMetadata,
)

/**
 * 伤害的暴击元数据.
 */
data class CriticalStrikeMetadata(
    /**
     * 暴击倍率的值.
     */
    val power: Double,

    /**
     * 这次伤害的暴击状态.
     */
    val state: CriticalStrikeState,
) {
    companion object Constants {
        /**
         * 默认的暴击元数据.
         * 用于不会暴击的攻击.
         */
        @JvmField
        val NONE: CriticalStrikeMetadata = CriticalStrikeMetadata(1.0, CriticalStrikeState.NONE)
    }
}

/**
 * 暴击状态.
 */
enum class CriticalStrikeState {
    /**
     * 正暴击.
     */
    POSITIVE,

    /**
     * 负暴击.
     */
    NEGATIVE,

    /**
     * 无暴击.
     */
    NONE
}

//<editor-fold desc="CriticalStrikeMetadata">
/**
 * 通过属性计算和随机过程创建一个 [CriticalStrikeMetadata].
 */
internal fun CriticalStrikeMetadata(chance: Double, positivePower: Double, negativePower: Double, nonePower: Double): CriticalStrikeMetadata {
    val power: Double
    val state: CriticalStrikeState
    if (chance < 0) {
        if (Random.nextDouble() < chance.absoluteValue) {
            state = CriticalStrikeState.NEGATIVE
            power = negativePower
        } else {
            state = CriticalStrikeState.NONE
            power = nonePower
        }
    } else {
        if (Random.nextDouble() < chance) {
            state = CriticalStrikeState.POSITIVE
            power = positivePower
        } else {
            state = CriticalStrikeState.NONE
            power = nonePower
        }
    }
    return CriticalStrikeMetadata(power, state)
}

internal fun CriticalStrikeMetadata(attributeMap: AttributeMap): CriticalStrikeMetadata {
    return CriticalStrikeMetadata(
        attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
        attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
        attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER),
        attributeMap.getValue(Attributes.NONE_CRITICAL_STRIKE_POWER)
    )
}
//</editor-fold>

//<editor-fold desc="DamageMetadata Constructors">
/**
 * 非生物造成的伤害元数据.
 * 用于给特定原版伤害类型加上元素和护甲穿透.
 * 如: 给溺水伤害加上水元素, 给着火伤害加上火元素.
 * 或用于无来源的弹射物的伤害.
 */
internal object VanillaDamageMetadata {
    operator fun invoke(damageBundle: DamageBundle): DamageMetadata {
        return DamageMetadata(
            damageTags = DamageTags(),
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata.NONE
        )
    }

    operator fun invoke(element: RegistryEntry<ElementType>, damageValue: Double, defensePenetration: Double, defensePenetrationRate: Double): DamageMetadata {
        return invoke(
            damageBundle {
                single(element) {
                    min(damageValue)
                    max(damageValue)
                    rate(1.0)
                    defensePenetration(defensePenetration)
                    defensePenetrationRate(defensePenetrationRate)
                }
            }
        )
    }

    operator fun invoke(damageValue: Double): DamageMetadata {
        return invoke(KoishRegistries.ELEMENT.getDefaultEntry(), damageValue, 0.0, 0.0)
    }
}

internal object PlayerDamageMetadata {
    /**
     * 玩家徒手造成的伤害.
     * 或使用非ATTACK, 相当于徒手攻击的物品造成的伤害.
     * 即 1 点默认元素伤害.
     */
    @JvmField
    val HAND_WITHOUT_ATTACK: DamageMetadata = DamageMetadata(
        damageTags = DamageTags(DamageTag.HAND),
        damageBundle = damageBundle {
            default {
                min(1.0)
                max(1.0)
                rate(1.0)
                defensePenetration(.0)
                defensePenetrationRate(.0)
            }
        },
        criticalStrikeMetadata = CriticalStrikeMetadata.NONE
    )

    operator fun invoke(user: User<Player>, damageBundle: DamageBundle, damageTags: DamageTags): DamageMetadata {
        return DamageMetadata(
            damageTags = damageTags,
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata(
                chance = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                positivePower = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
                negativePower = user.attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER),
                nonePower = user.attributeMap.getValue(Attributes.NONE_CRITICAL_STRIKE_POWER)
            )
        )
    }
}

internal object EntityDamageMetadata {
    operator fun invoke(damageBundle: DamageBundle, criticalStrikeMetadata: CriticalStrikeMetadata, damageTags: DamageTags): DamageMetadata {
        return DamageMetadata(
            damageTags = damageTags,
            damageBundle = damageBundle,
            criticalStrikeMetadata = criticalStrikeMetadata
        )
    }

    operator fun invoke(damageBundle: DamageBundle, criticalStrikeMetadata: CriticalStrikeMetadata): DamageMetadata {
        return invoke(damageBundle, criticalStrikeMetadata, DamageTags())
    }
}
//</editor-fold>

//<editor-fold desc="DamageMetadata Builder">
/**
 * 从配置文件反序列化得到的能够构建 [DamageMetadata] 的构造器.
 */
internal sealed interface DamageMetadataBuilder<T> {
    val damageTags: DamageTagsBuilder

    fun build(event: EntityDamageEvent): DamageMetadata
}

/**
 * 从配置文件反序列化得到的能够构建 [DamageTags] 的构造器.
 */
internal interface DamageTagsBuilder {
    val damageTags: List<DamageTag>

    fun build(): DamageTags
}

/**
 * 从配置文件反序列化得到的能够构建 [DamagePacket] 的构造器.
 */
internal interface DamagePacketBuilder<T> {
    val element: RegistryEntry<ElementType>
    val min: T
    val max: T
    val rate: T
    val defensePenetration: T
    val defensePenetrationRate: T

    fun build(): DamagePacket
}

/**
 * 从配置文件反序列化得到的能够构建 [CriticalStrikeMetadata] 的构造器.
 */
internal interface CriticalStrikeMetadataBuilder<T> {
    val chance: T
    val positivePower: T
    val negativePower: T
    val nonePower: T

    fun build(): CriticalStrikeMetadata
}

////// Direct
/**
 * 配置文件 **直接** 指定全部内容的 [DamageMetadata] 序列化器.
 */
@ConfigSerializable
internal data class DirectDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
    @Required
    val damageBundle: Map<String, DirectDamagePacketBuilder>,
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataBuilder,
) : DamageMetadataBuilder<Double> {

    override fun build(event: EntityDamageEvent): DamageMetadata {
        return build()
    }

    private fun build(): DamageMetadata {
        val damageTags = damageTags.build()
        val damageBundle = damageBundle.map { (element, packet) ->
            val element0: RegistryEntry<ElementType> = KoishRegistries.ELEMENT.getEntry(element) ?: KoishRegistries.ELEMENT.getDefaultEntry()
            val packet0 = packet.build()
            element0 to packet0
        }.toMap().let(::DamageBundle)
        val criticalStrikeMetadata = criticalStrikeMetadata.build()
        return DamageMetadata(damageTags, damageBundle, criticalStrikeMetadata)
    }
}

/**
 * 配置文件 **不指定伤害** 的 [DamageMetadata] 序列化器.
 * 只支持单元素.
 * 用于爆炸等伤害由原版决定的地方.
 */
@ConfigSerializable
internal data class VanillaDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataBuilder,
    @Required
    val element: RegistryEntry<ElementType>,
    val rate: Double = 1.0,
    val defensePenetration: Double = 0.0,
    val defensePenetrationRate: Double = 0.0,
) : DamageMetadataBuilder<Double> {

    override fun build(event: EntityDamageEvent): DamageMetadata {
        val damageTags = damageTags.build()
        val damageBundle = damageBundle {
            single(element) {
                min(event.damage)
                max(event.damage)
                rate(rate)
                defensePenetration(defensePenetration)
                defensePenetrationRate(defensePenetrationRate)
            }
        }
        val criticalStrikeMetadata = criticalStrikeMetadata.build()
        return DamageMetadata(damageTags, damageBundle, criticalStrikeMetadata)
    }
}

/**
 * 依赖攻击实体的 [cc.mewcraft.wakame.attribute.AttributeMap] 的 [DamageMetadata] 序列化器.
 */
@ConfigSerializable
internal data class AttributeDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
) : DamageMetadataBuilder<Double> {
    override fun build(event: EntityDamageEvent): DamageMetadata {
        val damager = event.damageSource.causingEntity ?: throw IllegalStateException(
            "Failed to build damage metadata by attribute map because the damager is null"
        )
        val attributeMap = AttributeMapAccess.instance().get(damager).getOrElse {
            error("Failed to build damage metadata by attribute map because the entity '${damager.type}' does not have an attribute map.")
        }
        val damageTags = damageTags.build()
        val damageBundle = damageBundle(attributeMap) {
            every { standard() }
        }
        val criticalStrikeMetadata = CriticalStrikeMetadata(attributeMap)
        return DamageMetadata(damageTags, damageBundle, criticalStrikeMetadata)
    }
}

@ConfigSerializable
internal data class DirectDamageTagsBuilder(
    @Required
    override val damageTags: List<DamageTag>,
) : DamageTagsBuilder {

    override fun build(): DamageTags {
        return DamageTags(damageTags)
    }
}

@ConfigSerializable
internal data class DirectDamagePacketBuilder(
    @NodeKey
    @Required
    override val element: RegistryEntry<ElementType>,
    @Required
    override val min: Double,
    @Required
    override val max: Double,
    override val rate: Double = 1.0,
    override val defensePenetration: Double = 0.0,
    override val defensePenetrationRate: Double = 0.0,
) : DamagePacketBuilder<Double> {

    override fun build(): DamagePacket {
        return DamagePacket(element, min, max, rate, defensePenetration, defensePenetrationRate)
    }
}

@ConfigSerializable
internal data class DirectCriticalStrikeMetadataBuilder(
    override val chance: Double = 0.0,
    override val positivePower: Double = 1.0,
    override val negativePower: Double = 1.0,
    override val nonePower: Double = 1.0,
) : CriticalStrikeMetadataBuilder<Double> {

    override fun build(): CriticalStrikeMetadata {
        return CriticalStrikeMetadata(chance, positivePower, negativePower, nonePower)
    }
}

@ConfigSerializable
internal data class MolangDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
    @Required
    val damageBundle: Map<String, MolangDamagePacketBuilder>,
    @Required
    val criticalStrikeMetadata: MolangCriticalStrikeMetadataBuilder,
) : DamageMetadataBuilder<Expression> {

    override fun build(event: EntityDamageEvent): DamageMetadata {
        return build()
    }

    fun build(): DamageMetadata {
        val damageTags = damageTags.build()
        val damageBundle = damageBundle.map { (element, packet) ->
            val element0: RegistryEntry<ElementType> = KoishRegistries.ELEMENT.getEntry(element) ?: KoishRegistries.ELEMENT.getDefaultEntry()
            val packet0 = packet.build()
            element0 to packet0
        }.toMap().let(::DamageBundle)
        val criticalStrikeState = criticalStrikeMetadata.build()
        return DamageMetadata(damageTags, damageBundle, criticalStrikeState)
    }
}

@ConfigSerializable
internal data class MolangDamagePacketBuilder(
    @NodeKey
    @Required
    override val element: RegistryEntry<ElementType>,
    @Required
    override val min: Expression,
    @Required
    override val max: Expression,
    @Required
    override val rate: Expression,
    @Required
    override val defensePenetration: Expression,
    @Required
    override val defensePenetrationRate: Expression,
) : DamagePacketBuilder<Expression> {
    override fun build(): DamagePacket {
        val engine = MochaEngine.createStandard()
        val element = element
        val min = min.evaluate(engine)
        val max = max.evaluate(engine)
        val rate = rate.evaluate(engine)
        val defensePenetration = defensePenetration.evaluate(engine)
        val defensePenetrationRate = defensePenetrationRate.evaluate(engine)
        return DamagePacket(element, min, max, rate, defensePenetration, defensePenetrationRate)
    }
}

@ConfigSerializable
internal data class MolangCriticalStrikeMetadataBuilder(
    @Required
    override val chance: Expression,
    @Required
    override val positivePower: Expression,
    @Required
    override val negativePower: Expression,
    @Required
    override val nonePower: Expression,
) : CriticalStrikeMetadataBuilder<Expression> {
    override fun build(): CriticalStrikeMetadata {
        val engine = MochaEngine.createStandard()
        return CriticalStrikeMetadata(
            chance.evaluate(engine),
            positivePower.evaluate(engine),
            negativePower.evaluate(engine),
            negativePower.evaluate(engine)
        )
    }
}
//</editor-fold>

internal object DamageMetadataBuilderSerializer : TypeSerializer<DamageMetadataBuilder<*>> {

    // FIXME 使用 DispatchingTypeSerializer 替代该实现
    private val TYPE_MAPPING: Map<String, KType> = mapOf(
        "direct" to typeOf<DirectDamageMetadataBuilder>(),
        "vanilla" to typeOf<VanillaDamageMetadataBuilder>(),
        "attribute" to typeOf<AttributeDamageMetadataBuilder>(),
        "molang" to typeOf<MolangDamageMetadataBuilder>(),
    )

    override fun deserialize(type: Type, node: ConfigurationNode): DamageMetadataBuilder<*> {
        val dataTypeId = node.node("type").getString("null")
        val dataType = TYPE_MAPPING[dataTypeId] ?: throw SerializationException("Unknown damage metadata builder type: '$dataTypeId'")
        return node.require(dataType)
    }

}