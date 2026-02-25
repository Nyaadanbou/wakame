package cc.mewcraft.wakame.item.data.impl

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import net.kyori.adventure.text.Component
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 用于向玩家展示“桶”里的实体信息.
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

    // 用于序列化
    val type: EntityBucketInfoType

    // 用于物品渲染
    val typeName: Component

    interface Ageable {
        val isAdult: Boolean
    }

    interface CollarColorable {
        val collarColor: String
    }

    interface Shearable {
        val readyToBeSheared: Boolean
    }

    interface Tameable {
        val ownerName: String?
    }

    interface Variable {
        val variant: String
    }
}

class EntityBucketInfoType(internal val kotlinType: KType)

object EntityBucketInfoTypes {
    //<editor-fold desc="Animals">
    @JvmField
    val ARMADILLO: EntityBucketInfoType = register<ArmadilloEntityBucketInfo>("armadillo")

    @JvmField
    val BEE: EntityBucketInfoType = register<BeeEntityBucketInfo>("bee")

    @JvmField
    val CAMEL: EntityBucketInfoType = register<CamelEntityBucketInfo>("camel")

    @JvmField
    val CAT: EntityBucketInfoType = register<CatEntityBucketInfo>("cat")

    @JvmField
    val CHICKEN: EntityBucketInfoType = register<ChickenEntityBucketInfo>("chicken")

    @JvmField
    val COW: EntityBucketInfoType = register<CowEntityBucketInfo>("cow")

    @JvmField
    val DOLPHIN: EntityBucketInfoType = register<DolphinEntityBucketInfo>("dolphin")

    @JvmField
    val DONKEY: EntityBucketInfoType = register<DonkeyEntityBucketInfo>("donkey")

    @JvmField
    val FOX: EntityBucketInfoType = register<FoxEntityBucketInfo>("fox")

    @JvmField
    val FROG: EntityBucketInfoType = register<FrogEntityBucketInfo>("frog")

    @JvmField
    val GLOW_SQUID: EntityBucketInfoType = register<GlowSquidEntityBucketInfo>("glow_squid")

    @JvmField
    val GOAT: EntityBucketInfoType = register<GoatEntityBucketInfo>("goat")

    @JvmField
    val HAPPY_GHAST: EntityBucketInfoType = register<HappyGhastEntityBucketInfo>("happy_ghast")

    @JvmField
    val HOGLIN: EntityBucketInfoType = register<HoglinEntityBucketInfo>("hoglin")

    @JvmField
    val HORSE: EntityBucketInfoType = register<HorseEntityBucketInfo>("horse")

    @JvmField
    val LLAMA: EntityBucketInfoType = register<LlamaEntityBucketInfo>("llama")

    @JvmField
    val MOOSHROOM: EntityBucketInfoType = register<MooshroomEntityBucketInfo>("mooshroom")

    @JvmField
    val MULE: EntityBucketInfoType = register<MuleEntityBucketInfo>("mule")

    @JvmField
    val OCELOT: EntityBucketInfoType = register<OcelotEntityBucketInfo>("ocelot")

    @JvmField
    val PANDA: EntityBucketInfoType = register<PandaEntityBucketInfo>("panda")

    @JvmField
    val PARROT: EntityBucketInfoType = register<ParrotEntityBucketInfo>("parrot")

    @JvmField
    val PIG: EntityBucketInfoType = register<PigEntityBucketInfo>("pig")

    @JvmField
    val POLAR_BEAR: EntityBucketInfoType = register<PolarBearEntityBucketInfo>("polar_bear")

    @JvmField
    val RABBIT: EntityBucketInfoType = register<RabbitEntityBucketInfo>("rabbit")

    @JvmField
    val SHEEP: EntityBucketInfoType = register<SheepEntityBucketInfo>("sheep")

    @JvmField
    val SKELETON_HORSE: EntityBucketInfoType = register<SkeletonHorseEntityBucketInfo>("skeleton_horse")

    @JvmField
    val SNIFFER: EntityBucketInfoType = register<SnifferEntityBucketInfo>("sniffer")

    @JvmField
    val SQUID: EntityBucketInfoType = register<SquidEntityBucketInfo>("squid")

    @JvmField
    val STRIDER: EntityBucketInfoType = register<StriderEntityBucketInfo>("strider")

    @JvmField
    val TRADER_LLAMA: EntityBucketInfoType = register<TraderLlamaEntityBucketInfo>("trader_llama")

    @JvmField
    val TURTLE: EntityBucketInfoType = register<TurtleEntityBucketInfo>("turtle")

    @JvmField
    val WOLF: EntityBucketInfoType = register<WolfEntityBucketInfo>("wolf")

    @JvmField
    val ZOMBIE_HORSE: EntityBucketInfoType = register<ZombieHorseEntityBucketInfo>("zombie_horse")
    //</editor-fold>

    //<editor-fold desc="Animals Like"> See: https://minecraft.wiki/w/Animal
    @JvmField
    val NAUTILUS: EntityBucketInfoType = register<NautilusEntityBucketInfo>("nautilus")

    @JvmField
    val ALLAY: EntityBucketInfoType = register<AllayEntityBucketInfo>("allay")

    @JvmField
    val IRON_GOLEM: EntityBucketInfoType = register<IronGolemEntityBucketInfo>("iron_golem")

    @JvmField
    val SNOW_GOLEM: EntityBucketInfoType = register<SnowGolemEntityBucketInfo>("snow_golem")
    //</editor-fold>

    //<editor-fold desc="NPCs">
    @JvmField
    val VILLAGER: EntityBucketInfoType = register<VillagerEntityBucketInfo>("villager")

    @JvmField
    val WANDERING_TRADER: EntityBucketInfoType = register<WanderingTraderEntityBucketInfo>("wandering_trader")

    @JvmField
    val ZOMBIE_VILLAGER: EntityBucketInfoType = register<ZombieVillagerEntityBucketInfo>("zombie_villager")

    @JvmField
    val ZOMBIE_NAUTILUS: EntityBucketInfoType = register<ZombieNautilusEntityBucketInfo>("zombie_nautilus")
    //</editor-fold>

    private inline fun <reified E : EntityBucketInfo> register(id: String): EntityBucketInfoType {
        return Registry.register(BuiltInRegistries.ENTITY_BUCKET_INFO_TYPE, id, EntityBucketInfoType(typeOf<E>()))
    }
}

//<editor-fold desc="Animals">
@ConfigSerializable
data class ArmadilloEntityBucketInfo(
    val state: String = "none",
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ARMADILLO
    override val typeName: Component get() = Component.translatable(EntityType.ARMADILLO)
}

@ConfigSerializable
data class BeeEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.BEE
    override val typeName: Component get() = Component.translatable(EntityType.BEE)
}

@ConfigSerializable
data class CamelEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val ownerName: String? = null,
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Tameable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.CAMEL
    override val typeName: Component get() = Component.translatable(EntityType.CAMEL)
}

@ConfigSerializable
data class CatEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val collarColor: String = "none",
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.CollarColorable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.CAT
    override val typeName: Component get() = Component.translatable(EntityType.CAT)
}

@ConfigSerializable
data class ChickenEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.CHICKEN
    override val typeName: Component get() = Component.translatable(EntityType.CHICKEN)
}

@ConfigSerializable
data class CowEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.COW
    override val typeName: Component get() = Component.translatable(EntityType.COW)
}

@ConfigSerializable
data class DolphinEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.DOLPHIN
    override val typeName: Component get() = Component.translatable(EntityType.DOLPHIN)
}

@ConfigSerializable
data class DonkeyEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val ownerName: String? = null,
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Tameable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.DONKEY
    override val typeName: Component get() = Component.translatable(EntityType.DONKEY)
}

@ConfigSerializable
data class FoxEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.FOX
    override val typeName: Component get() = Component.translatable(EntityType.FOX)
}

@ConfigSerializable
data class FrogEntityBucketInfo(
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.FROG
    override val typeName: Component get() = Component.translatable(EntityType.FROG)
}

@ConfigSerializable
data class GlowSquidEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.GLOW_SQUID
    override val typeName: Component get() = Component.translatable(EntityType.GLOW_SQUID)
}

@ConfigSerializable
data class GoatEntityBucketInfo(
    val hasLeftHorn: Boolean = false,
    val hasRightHorn: Boolean = false,
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.GOAT
    override val typeName: Component get() = Component.translatable(EntityType.GOAT)
}

@ConfigSerializable
data class HappyGhastEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.HAPPY_GHAST
    override val typeName: Component get() = Component.translatable(EntityType.HAPPY_GHAST)
}

@ConfigSerializable
data class HoglinEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.HOGLIN
    override val typeName: Component get() = Component.translatable(EntityType.HOGLIN)
}

@ConfigSerializable
data class HorseEntityBucketInfo(
    override val ownerName: String? = null,
) : EntityBucketInfo, EntityBucketInfo.Tameable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.HORSE
    override val typeName: Component get() = Component.translatable(EntityType.HORSE)
}

@ConfigSerializable
data class LlamaEntityBucketInfo(
    override val variant: String = "none",
    override val ownerName: String? = null,
) : EntityBucketInfo, EntityBucketInfo.Variable, EntityBucketInfo.Tameable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.LLAMA
    override val typeName: Component get() = Component.translatable(EntityType.LLAMA)
}

@ConfigSerializable
data class MooshroomEntityBucketInfo(
    override val isAdult: Boolean,
    override val readyToBeSheared: Boolean = false,
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Shearable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.MOOSHROOM
    override val typeName: Component get() = Component.translatable(EntityType.MOOSHROOM)
}

@ConfigSerializable
data class MuleEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.MULE
    override val typeName: Component get() = Component.translatable(EntityType.MULE)
}

@ConfigSerializable
data class OcelotEntityBucketInfo(
    val trusting: Boolean = false,
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.OCELOT
    override val typeName: Component get() = Component.translatable(EntityType.OCELOT)
}

@ConfigSerializable
data class PandaEntityBucketInfo(
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.PANDA
    override val typeName: Component get() = Component.translatable(EntityType.PANDA)
}

@ConfigSerializable
data class ParrotEntityBucketInfo(
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.PARROT
    override val typeName: Component get() = Component.translatable(EntityType.PARROT)
}

@ConfigSerializable
data class PigEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.PIG
    override val typeName: Component get() = Component.translatable(EntityType.PIG)
}

@ConfigSerializable
data class PolarBearEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.POLAR_BEAR
    override val typeName: Component get() = Component.translatable(EntityType.POLAR_BEAR)
}

@ConfigSerializable
data class RabbitEntityBucketInfo(
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.RABBIT
    override val typeName: Component get() = Component.translatable(EntityType.RABBIT)
}

@ConfigSerializable
data class SheepEntityBucketInfo(
    override val readyToBeSheared: Boolean = false,
    override val variant: String = "none",
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Shearable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SHEEP
    override val typeName: Component get() = Component.translatable(EntityType.SHEEP)
}

@ConfigSerializable
data class SkeletonHorseEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SKELETON_HORSE
    override val typeName: Component get() = Component.translatable(EntityType.SKELETON_HORSE)
}

@ConfigSerializable
data class SnifferEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SNIFFER
    override val typeName: Component get() = Component.translatable(EntityType.SNIFFER)
}

@ConfigSerializable
data class SquidEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SQUID
    override val typeName: Component get() = Component.translatable(EntityType.SQUID)
}

@ConfigSerializable
data class StriderEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.STRIDER
    override val typeName: Component get() = Component.translatable(EntityType.STRIDER)
}

@ConfigSerializable
data class TraderLlamaEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val variant: String = "none",
    override val ownerName: String? = null,
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.Variable, EntityBucketInfo.Tameable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.TRADER_LLAMA
    override val typeName: Component get() = Component.translatable(EntityType.TRADER_LLAMA)
}

@ConfigSerializable
data class TurtleEntityBucketInfo(
    val hasEgg: Boolean = false,
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.TURTLE
    override val typeName: Component get() = Component.translatable(EntityType.TURTLE)
}

@ConfigSerializable
data class WolfEntityBucketInfo(
    override val isAdult: Boolean = false,
    override val collarColor: String = "none",
    override val ownerName: String? = null,
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Ageable, EntityBucketInfo.CollarColorable, EntityBucketInfo.Tameable, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.WOLF
    override val typeName: Component get() = Component.translatable(EntityType.WOLF)
}

@ConfigSerializable
data class ZombieHorseEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ZOMBIE_HORSE
    override val typeName: Component get() = Component.translatable(EntityType.ZOMBIE_HORSE)
}
//</editor-fold>

//<editor-fold desc="Animals Like">
@ConfigSerializable
data class NautilusEntityBucketInfo(
    override val isAdult: Boolean = false,
) : EntityBucketInfo, EntityBucketInfo.Ageable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.NAUTILUS
    override val typeName: Component get() = Component.translatable(EntityType.NAUTILUS)
}

@ConfigSerializable
data class AllayEntityBucketInfo(
    val itemInMainhand: String? = null,
    val itemInOffhand: String? = null,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ALLAY
    override val typeName: Component get() = Component.translatable(EntityType.ALLAY)
}

@ConfigSerializable
data class IronGolemEntityBucketInfo(
    val isPlayerCreated: Boolean = false,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.IRON_GOLEM
    override val typeName: Component get() = Component.translatable(EntityType.IRON_GOLEM)
}

@ConfigSerializable
data class SnowGolemEntityBucketInfo(
    val hasPumpkin: Boolean = false,
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.SNOW_GOLEM
    override val typeName: Component get() = Component.translatable(EntityType.SNOW_GOLEM)
}
//</editor-fold>

//<editor-fold desc="NPCs">
@ConfigSerializable
data class VillagerEntityBucketInfo(
    val level: Int = 0,
    val region: String = "none",
    val profession: String = "none",
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.VILLAGER
    override val typeName: Component get() = Component.translatable(EntityType.VILLAGER)
}

@ConfigSerializable
data class WanderingTraderEntityBucketInfo(
    val offers: List<String> = listOf(),
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.WANDERING_TRADER
    override val typeName: Component get() = Component.translatable(EntityType.WANDERING_TRADER)
}

@ConfigSerializable
data class ZombieVillagerEntityBucketInfo(
    val region: String = "none",
    val profession: String = "none",
) : EntityBucketInfo {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ZOMBIE_VILLAGER
    override val typeName: Component get() = Component.translatable(EntityType.ZOMBIE_VILLAGER)
}

@ConfigSerializable
data class ZombieNautilusEntityBucketInfo(
    override val variant: String = "none",
) : EntityBucketInfo, EntityBucketInfo.Variable {
    override val type: EntityBucketInfoType get() = EntityBucketInfoTypes.ZOMBIE_NAUTILUS
    override val typeName: Component get() = Component.translatable(EntityType.ZOMBIE_NAUTILUS)
}
//</editor-fold>