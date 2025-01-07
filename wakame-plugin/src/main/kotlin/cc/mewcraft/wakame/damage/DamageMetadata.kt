@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.core.Holder
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.registries.KoishRegistries
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.krequire
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.reflect.KType
import kotlin.reflect.typeOf

//<editor-fold desc="CriticalStrikeMetadata">
/**
 * 通过属性计算和随机过程创建一个 [CriticalStrikeMetadata].
 */
fun CriticalStrikeMetadata(chance: Double, positivePower: Double, negativePower: Double, nonePower: Double): CriticalStrikeMetadata {
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

fun CriticalStrikeMetadata(attributeMap: AttributeMap): CriticalStrikeMetadata {
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
object VanillaDamageMetadata {
    operator fun invoke(damageBundle: DamageBundle): DamageMetadata {
        return DamageMetadata(
            damageTags = DamageTags(),
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata.NONE
        )
    }

    operator fun invoke(element: Holder<Element>, damageValue: Double, defensePenetration: Double, defensePenetrationRate: Double): DamageMetadata {
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
        return invoke(KoishRegistries.ELEMENT.defaultValue, damageValue, 0.0, 0.0)
    }
}

object PlayerDamageMetadata {
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

object EntityDamageMetadata {
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
sealed interface DamageMetadataBuilder<T> {
    val damageTags: DamageTagsBuilder

    fun build(event: EntityDamageEvent): DamageMetadata
}

/**
 * 从配置文件反序列化得到的能够构建 [DamageTags] 的构造器.
 */
interface DamageTagsBuilder {
    val damageTags: List<DamageTag>

    fun build(): DamageTags
}

/**
 * 从配置文件反序列化得到的能够构建 [DamagePacket] 的构造器.
 */
interface DamagePacketBuilder<T> {
    val element: Holder<Element>
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
interface CriticalStrikeMetadataBuilder<T> {
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
data class DirectDamageMetadataBuilder(
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
            val element0 = KoishRegistries.ELEMENT.getValueOrDefault(element)
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
data class VanillaDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataBuilder,
    @Required
    val element: Holder<Element>,
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
data class AttributeDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
) : DamageMetadataBuilder<Double>, KoinComponent {
    override fun build(event: EntityDamageEvent): DamageMetadata {
        val damager = event.damageSource.causingEntity ?: throw IllegalStateException(
            "Failed to build damage metadata by attribute map because the damager is null"
        )
        val attributeMap = AttributeMapAccess.get(damager).getOrElse {
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
data class DirectDamageTagsBuilder(
    @Required
    override val damageTags: List<DamageTag>,
) : DamageTagsBuilder {

    override fun build(): DamageTags {
        return DamageTags(damageTags)
    }
}

@ConfigSerializable
data class DirectDamagePacketBuilder(
    @NodeKey
    @Required
    override val element: Holder<Element>,
    @Required
    override val min: Double,
    @Required
    override val max: Double,
    override val rate: Double = 1.0,
    override val defensePenetration: Double = 0.0,
    override val defensePenetrationRate: Double = 0.0,
) : DamagePacketBuilder<Double> {

    override fun build(): DamagePacket {
        return DamagePacket(element.value, min, max, rate, defensePenetration, defensePenetrationRate)
    }
}

@ConfigSerializable
data class DirectCriticalStrikeMetadataBuilder(
    override val chance: Double = 0.0,
    override val positivePower: Double = 1.0,
    override val negativePower: Double = 1.0,
    override val nonePower: Double = 1.0,
) : CriticalStrikeMetadataBuilder<Double> {

    override fun build(): CriticalStrikeMetadata {
        return CriticalStrikeMetadata(chance, positivePower, negativePower, nonePower)
    }
}

////// Molang
@ConfigSerializable
data class MolangDamageMetadataBuilder(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsBuilder,
    @Required
    val damageBundle: Map<String, MolangDamagePacketBuilder>,
    @Required
    val criticalStrikeMetadata: MolangCriticalStrikeMetadataBuilder,
) : DamageMetadataBuilder<Evaluable<*>> {

    override fun build(event: EntityDamageEvent): DamageMetadata {
        return build()
    }

    fun build(): DamageMetadata {
        val damageTags = damageTags.build()
        val damageBundle = damageBundle.map { (element, packet) ->
            val element0 = KoishRegistries.ELEMENT.getValueOrDefault(element)
            val packet0 = packet.build()
            element0 to packet0
        }.toMap().let(::DamageBundle)
        val criticalStrikeState = criticalStrikeMetadata.build()
        return DamageMetadata(damageTags, damageBundle, criticalStrikeState)
    }
}

@ConfigSerializable
data class MolangDamagePacketBuilder(
    @NodeKey
    @Required
    override val element: Holder<Element>,
    @Required
    override val min: Evaluable<*>,
    @Required
    override val max: Evaluable<*>,
    @Required
    override val rate: Evaluable<*>,
    @Required
    override val defensePenetration: Evaluable<*>,
    @Required
    override val defensePenetrationRate: Evaluable<*>,
) : DamagePacketBuilder<Evaluable<*>> {
    override fun build(): DamagePacket {
        val engine = MochaEngine.createStandard()
        val element = element.value
        val min = min.evaluate(engine)
        val max = max.evaluate(engine)
        val rate = rate.evaluate(engine)
        val defensePenetration = defensePenetration.evaluate(engine)
        val defensePenetrationRate = defensePenetrationRate.evaluate(engine)
        return DamagePacket(element, min, max, rate, defensePenetration, defensePenetrationRate)
    }
}

@ConfigSerializable
data class MolangCriticalStrikeMetadataBuilder(
    @Required
    override val chance: Evaluable<*>,
    @Required
    override val positivePower: Evaluable<*>,
    @Required
    override val negativePower: Evaluable<*>,
    @Required
    override val nonePower: Evaluable<*>,
) : CriticalStrikeMetadataBuilder<Evaluable<*>> {
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
    private val TYPE_MAPPING: Map<String, KType> = mapOf(
        "direct" to typeOf<DirectDamageMetadataBuilder>(),
        "vanilla" to typeOf<VanillaDamageMetadataBuilder>(),
        "attribute" to typeOf<AttributeDamageMetadataBuilder>(),
        "molang" to typeOf<MolangDamageMetadataBuilder>(),
    )

    override fun deserialize(type: Type, node: ConfigurationNode): DamageMetadataBuilder<*> {
        val key = node.node("type").getString("null")
        val kType = TYPE_MAPPING[key] ?: throw SerializationException("Unknown damage metadata builder type: '$key'")
        return node.krequire(kType)
    }

    override fun serialize(type: Type, obj: DamageMetadataBuilder<*>?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}