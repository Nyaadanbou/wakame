package cc.mewcraft.wakame.damage

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.molang.Expression
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 从配置文件反序列化得到的能够构建 [DamageMetadata] 的构造器.
 */
internal sealed interface DamageMetadataBuilder<T> {

    fun build(context: RawDamageContext): DamageMetadata

    companion object {
        fun serializer(): TypeSerializer<DamageMetadataBuilder<*>> = object : SimpleSerializer<DamageMetadataBuilder<*>> {

            // FIXME 使用 DispatchingSerializer 替代该实现
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
    }
}

/**
 * 从配置文件反序列化得到的能够构建 [DamagePacket] 的构造器.
 */
internal interface DamagePacketBuilder<T> {
    val element: RegistryEntry<Element>
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

/**
 * 配置文件 *直接* 指定全部内容的 [DamageMetadata] 序列化器.
 */
@ConfigSerializable
internal data class DirectDamageMetadataBuilder(
    @Required
    val damageBundle: Map<String, DirectDamagePacketBuilder>,
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataBuilder,
) : DamageMetadataBuilder<Double> {

    override fun build(context: RawDamageContext): DamageMetadata {
        return build()
    }

    private fun build(): DamageMetadata {
        val damageBundle = damageBundle.map { (xelement, xpacket) ->
            val element: RegistryEntry<Element> = BuiltInRegistries.ELEMENT.getEntry(xelement) ?: BuiltInRegistries.ELEMENT.getDefaultEntry()
            val packet: DamagePacket = xpacket.build()
            element to packet
        }.toMap().let(DamageBundle::damageBundleOf)
        val criticalStrikeMetadata = criticalStrikeMetadata.build()
        return DamageMetadata(damageBundle, criticalStrikeMetadata)
    }
}

/**
 * 配置文件 *不指定伤害数值* 的 [DamageMetadata] 序列化器. 只支持单元素.
 *
 * 专用于爆炸等伤害由原版决定的地方.
 */
@ConfigSerializable
internal data class VanillaDamageMetadataBuilder(
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataBuilder,
    @Required
    val element: RegistryEntry<Element>,
    val rate: Double = 1.0,
    val defensePenetration: Double = 0.0,
    val defensePenetrationRate: Double = 0.0,
) : DamageMetadataBuilder<Double> {

    override fun build(context: RawDamageContext): DamageMetadata {
        val damage = context.damage
        val damageBundle = damageBundle {
            single(element) {
                min(damage)
                max(damage)
                rate(rate)
                defensePenetration(defensePenetration)
                defensePenetrationRate(defensePenetrationRate)
            }
        }
        val criticalStrikeMetadata = criticalStrikeMetadata.build()
        return DamageMetadata(damageBundle, criticalStrikeMetadata)
    }
}

/**
 * 依赖攻击实体的 [cc.mewcraft.wakame.entity.attribute.AttributeMap] 的 [DamageMetadata] 序列化器.
 */
@ConfigSerializable
internal object AttributeDamageMetadataBuilder : DamageMetadataBuilder<Double> {

    override fun build(context: RawDamageContext): DamageMetadata {
        val damager = context.damageSource.causingEntity ?: throw IllegalStateException(
            "Failed to build damage metadata by attribute map because the damager is null."
        )
        val attributeMap = AttributeMapAccess.INSTANCE.get(damager).getOrElse {
            error("Failed to build damage metadata by attribute map because the entity '${damager.type}' does not have an attribute map.")
        }
        val damageBundle = damageBundle(attributeMap) {
            every {
                standard()
            }
        }
        val criticalStrikeMetadata = CriticalStrikeMetadata(attributeMap)
        return DamageMetadata(damageBundle, criticalStrikeMetadata)
    }
}

@ConfigSerializable
internal data class DirectDamagePacketBuilder(
    @NodeKey
    @Required
    override val element: RegistryEntry<Element>,
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
    @Required
    val damageBundle: Map<String, MolangDamagePacketBuilder>,
    @Required
    val criticalStrikeMetadata: MolangCriticalStrikeMetadataBuilder,
) : DamageMetadataBuilder<Expression> {

    override fun build(context: RawDamageContext): DamageMetadata {
        return build()
    }

    fun build(): DamageMetadata {
        val damageBundle = damageBundle.map { (xelement, xpacket) ->
            val element: RegistryEntry<Element> = BuiltInRegistries.ELEMENT.getEntry(xelement) ?: BuiltInRegistries.ELEMENT.getDefaultEntry()
            val packet: DamagePacket = xpacket.build()
            element to packet
        }.toMap().let(DamageBundle::damageBundleOf)
        val criticalStrikeState = criticalStrikeMetadata.build()
        return DamageMetadata(damageBundle, criticalStrikeState)
    }
}

@ConfigSerializable
internal data class MolangDamagePacketBuilder(
    @NodeKey
    @Required
    override val element: RegistryEntry<Element>,
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
