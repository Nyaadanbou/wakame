package cc.mewcraft.wakame.item2.data.impl

import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 用于向玩家展示实体信息.
 */
sealed interface EntityBucketInfo {

    companion object {
        fun serializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<EntityBucketInfoType>(BuiltInRegistries.ENTITY_BUCKET_INFO_TYPE.valueByNameTypeSerializer())
            serials.registerExact<EntityBucketInfo>(DispatchingSerializer.create(EntityBucketInfo::type, EntityBucketInfoType::kotlinType))
            return serials.build()
        }
    }

    val type: EntityBucketInfoType
}

class EntityBucketInfoType(internal val kotlinType: KType)

object EntityBucketInfoTypes {
    @JvmField
    val ALLAY: EntityBucketInfoType = register<AllayEntityBucketInfo>("allay")

    @JvmField
    val ARMADILLO: EntityBucketInfoType = register<ArmadilloEntityBucketInfo>("armadillo")

    @JvmField
    val BAT: EntityBucketInfoType = register<BatEntityBucketInfo>("bat")

    @JvmField
    val CAMEL: EntityBucketInfoType = register<CamelEntityBucketInfo>("camel")

    @JvmField
    val CAT: EntityBucketInfoType = register<CatEntityBucketInfo>("cat")

    @JvmField
    val CHICKEN: EntityBucketInfoType = register<ChickenEntityBucketInfo>("chicken")

    @JvmField
    val COW: EntityBucketInfoType = register<CowEntityBucketInfo>("cow")

    // Donkey
    // Frog
    // Glow Squid
    // Happy Ghast
    // Horse

    @JvmField
    val MOOSHROOM: EntityBucketInfoType = register<MooshroomEntityBucketInfo>("mooshroom")

    // Mule

    @JvmField
    val OCELOT: EntityBucketInfoType = register<OcelotEntityBucketInfo>("ocelot")

    // Parrot

    @JvmField
    val PIG: EntityBucketInfoType = register<PigEntityBucketInfo>("pig")

    @JvmField
    val RABBIT: EntityBucketInfoType = register<RabbitEntityBucketInfo>("rabbit")

    @JvmField
    val SHEEP: EntityBucketInfoType = register<SheepEntityBucketInfo>("sheep")

    // Skeleton Horse
    // Sniffer

    @JvmField
    val SNOW_GOLEM: EntityBucketInfoType = register<SnowGolemEntityBucketInfo>("snow_golem")

    // Squid
    // Strider

    @JvmField
    val TURTLE: EntityBucketInfoType = register<TurtleEntityBucketInfo>("turtle")

    @JvmField
    val VILLAGER: EntityBucketInfoType = register<VillagerEntityBucketInfo>("villager")

    @JvmField
    val WANDERING_TRADER: EntityBucketInfoType = register<WanderingTraderEntityBucketInfo>("wandering_trader")

    private inline fun <reified E : EntityBucketInfo> register(id: String): EntityBucketInfoType {
        return Registry.register(BuiltInRegistries.ENTITY_BUCKET_INFO_TYPE, id, EntityBucketInfoType(typeOf<E>()))
    }
}

@ConfigSerializable
data class AllayEntityBucketInfo(
    val item: String? = null,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ALLAY
}

@ConfigSerializable
data class ArmadilloEntityBucketInfo(
    val scuteTime: Int,
    val state: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ARMADILLO
}

@ConfigSerializable
data class BatEntityBucketInfo(
    val hanging: Boolean,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.BAT
}

@ConfigSerializable
data class CamelEntityBucketInfo(
    val owner: String? = null,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.CAMEL
}

@ConfigSerializable
data class CatEntityBucketInfo(
    val collar: Boolean,
    val variant: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.CAT
}

@ConfigSerializable
data class ChickenEntityBucketInfo(
    val variant: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.CHICKEN
}

@ConfigSerializable
data class CowEntityBucketInfo(
    val variant: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.COW
}

// Donkey

// Frog

// Glow Squid

// Happy Ghast

// Horse

@ConfigSerializable
data class MooshroomEntityBucketInfo(
    val variant: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.MOOSHROOM
}

// Mule

@ConfigSerializable
data class OcelotEntityBucketInfo(
    val trusting: Boolean,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.OCELOT
}

// Parrot

@ConfigSerializable
data class PigEntityBucketInfo(
    val variant: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.PIG
}

@ConfigSerializable
data class RabbitEntityBucketInfo(
    val variant: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.RABBIT
}

@ConfigSerializable
data class SheepEntityBucketInfo(
    val variant: String,
    val sheared: Boolean,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SHEEP
}

// Skeleton Horse

// Sniffer

@ConfigSerializable
data class SnowGolemEntityBucketInfo(
    val pumpkin: Boolean,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SNOW_GOLEM
}

// Squid

// Strider

@ConfigSerializable
data class TurtleEntityBucketInfo(
    val hasEgg: Boolean,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.TURTLE
}

@ConfigSerializable
data class VillagerEntityBucketInfo(
    val level: Int,
    val region: String,
    val profession: String,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.VILLAGER
}

@ConfigSerializable
data class WanderingTraderEntityBucketInfo(
    val offers: List<String> = listOf(),
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.WANDERING_TRADER
}