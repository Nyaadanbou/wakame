package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import team.unnamed.mocha.MochaEngine

/* Implementations */

/**
 * 伤害元数据, 包含了一次伤害中"攻击阶段"的有关信息.
 * 一旦实例化后, 攻击伤害的数值以及各种信息就已经确定了.
 */
data class DamageMetadata(
    val damageTags: DamageTags,
    val damageBundle: DamageBundle,
    val criticalStrikeMetadata: CriticalStrikeMetadata,
)

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
            criticalStrikeMetadata = CriticalStrikeMetadata.DEFAULT
        )
    }

    operator fun invoke(element: Element, damageValue: Double, defensePenetration: Double, defensePenetrationRate: Double): DamageMetadata {
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
        return invoke(ElementRegistry.DEFAULT, damageValue, 0.0, 0.0)
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
        criticalStrikeMetadata = CriticalStrikeMetadata.DEFAULT
    )

    operator fun invoke(user: User<Player>, damageBundle: DamageBundle, damageTags: DamageTags): DamageMetadata {
        return DamageMetadata(
            damageTags = damageTags,
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata(
                chance = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                positivePower = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
                negativePower = user.attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER),
                nonePower = 1.0 // TODO 新属性 NONE_POWER
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

//<editor-fold desc="DamageMetadata Serializable">
interface DamageMetadataSerializable<T> {
    val damageTags: DamageTagsSerializable
    val damageBundle: Map<String, DamagePacketSerializable<T>> // map: element unique id -> damage packet (serializable)
    val criticalStrikeMetadata: CriticalStrikeMetadataSerializable<T>
}

interface DamageTagsSerializable {
    val damageTags: List<DamageTag>
}

interface DamagePacketSerializable<T> {
    val element: String
    val min: T
    val max: T
    val rate: T
    val defensePenetration: T
    val defensePenetrationRate: T
}

interface CriticalStrikeMetadataSerializable<T> {
    val chance: T
    val positivePower: T
    val negativePower: T
    val nonePower: T
}

////// Direct
@ConfigSerializable
data class DirectDamageMetadataSerializable(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsSerializable,
    @Required
    override val damageBundle: Map<String, DirectDamagePacketSerializable>,
    @Required
    override val criticalStrikeMetadata: DirectCriticalStrikeMetadataSerializable,
) : DamageMetadataSerializable<Double> {
    fun decode(): DamageMetadata {
        val damageTags = damageTags.decode()
        val damageBundle = damageBundle.map { (element, packet) ->
            val element0 = ElementRegistry.INSTANCES[element]
            val packet0 = packet.decode()
            element0 to packet0
        }.toMap().let(::DamageBundle)
        val criticalStrikeMetadata = criticalStrikeMetadata.decode()
        return DamageMetadata(damageTags, damageBundle, criticalStrikeMetadata)
    }
}

@ConfigSerializable
data class DirectDamageTagsSerializable(
    @Required
    override val damageTags: List<DamageTag>,
) : DamageTagsSerializable {
    fun decode(): DamageTags {
        return DamageTags(damageTags)
    }
}

@ConfigSerializable
data class DirectDamagePacketSerializable(
    @NodeKey
    @Required
    override val element: String,
    @Required
    override val min: Double,
    @Required
    override val max: Double,
    override val rate: Double = 1.0,
    override val defensePenetration: Double = 0.0,
    override val defensePenetrationRate: Double = 0.0,
) : DamagePacketSerializable<Double> {
    fun decode(): DamagePacket {
        val element = ElementRegistry.INSTANCES[element]
        return DamagePacket(element, min, max, rate, defensePenetration, defensePenetrationRate)
    }
}

@ConfigSerializable
data class DirectCriticalStrikeMetadataSerializable(
    override val chance: Double = 0.0,
    override val positivePower: Double = 1.0,
    override val negativePower: Double = 1.0,
    override val nonePower: Double = 1.0,
) : CriticalStrikeMetadataSerializable<Double> {
    fun decode(): CriticalStrikeMetadata {
        return CriticalStrikeMetadata(chance, positivePower, negativePower, nonePower)
    }
}

////// Molang
@ConfigSerializable
data class MolangDamageMetadataSerializable(
    @Setting(nodeFromParent = true)
    @Required
    override val damageTags: DirectDamageTagsSerializable,
    @Required
    override val damageBundle: Map<String, MolangDamagePacketSerializable>,
    @Required
    override val criticalStrikeMetadata: MolangCriticalStrikeMetadataSerializable,
) : DamageMetadataSerializable<Evaluable<*>> {
    fun decode(engine: MochaEngine<*>): DamageMetadata {
        val damageTags = damageTags.decode()
        val damageBundle = damageBundle.map { (element, packet) ->
            val element0 = ElementRegistry.INSTANCES[element]
            val packet0 = packet.decode(engine)
            element0 to packet0
        }.toMap().let(::DamageBundle)
        val criticalStrikeState = criticalStrikeMetadata.decode(engine)
        return DamageMetadata(damageTags, damageBundle, criticalStrikeState)
    }
}

@ConfigSerializable
data class MolangDamagePacketSerializable(
    @NodeKey
    @Required
    override val element: String,
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
) : DamagePacketSerializable<Evaluable<*>> {
    fun decode(engine: MochaEngine<*>): DamagePacket {
        val element = ElementRegistry.INSTANCES[element]
        val min = min.evaluate(engine)
        val max = max.evaluate(engine)
        val rate = rate.evaluate(engine)
        val defensePenetration = defensePenetration.evaluate(engine)
        val defensePenetrationRate = defensePenetrationRate.evaluate(engine)
        return DamagePacket(element, min, max, rate, defensePenetration, defensePenetrationRate)
    }
}

@ConfigSerializable
data class MolangCriticalStrikeMetadataSerializable(
    @Required
    override val chance: Evaluable<*>,
    @Required
    override val positivePower: Evaluable<*>,
    @Required
    override val negativePower: Evaluable<*>,
    @Required
    override val nonePower: Evaluable<*>,
) : CriticalStrikeMetadataSerializable<Evaluable<*>> {
    fun decode(engine: MochaEngine<*>): CriticalStrikeMetadata {
        return CriticalStrikeMetadata(
            chance.evaluate(engine),
            positivePower.evaluate(engine),
            negativePower.evaluate(engine),
            negativePower.evaluate(engine)
        )
    }
}
//</editor-fold>
