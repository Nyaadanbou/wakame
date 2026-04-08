package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface EnchantSlotCapacity {

    companion object {
        fun serializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<EnchantSlotCapacityType>(BuiltInRegistries.ENCHANT_SLOT_CAPACITY_TYPE.valueByNameTypeSerializer())
            serials.registerExact<EnchantSlotCapacity>(DispatchingSerializer.create(EnchantSlotCapacity::type, EnchantSlotCapacityType::kotlinType))
            return serials.build()
        }
    }

    val type: EnchantSlotCapacityType

    fun getCapacity(enchantment: Enchantment): Int
}

class EnchantSlotCapacityType(internal val kotlinType: KType)

object EnchantSlotCapacityTypes {

    @JvmField
    val LOCAL: EnchantSlotCapacityType = register<LocalEnchantSlotCapacity>("local")

    @JvmField
    val REFERENCE: EnchantSlotCapacityType = register<ReferenceEnchantSlotCapacity>("reference")

    private inline fun <reified E : EnchantSlotCapacity> register(id: String): EnchantSlotCapacityType {
        return Registry.register(BuiltInRegistries.ENCHANT_SLOT_CAPACITY_TYPE, id, EnchantSlotCapacityType(typeOf<E>()))
    }
}

@ConfigSerializable
data class LocalEnchantSlotCapacity(
    @Setting(value = "value")
    val config: Map<Key, Int>,
) : EnchantSlotCapacity {
    override val type: EnchantSlotCapacityType get() = EnchantSlotCapacityTypes.LOCAL

    override fun getCapacity(enchantment: Enchantment): Int {
        return config[enchantment.key] ?: 1 // 不存在则返回 1
    }
}

@ConfigSerializable
data class ReferenceEnchantSlotCapacity(
    @Setting(value = "value")
    val reference: String,
) : EnchantSlotCapacity {
    override val type: EnchantSlotCapacityType get() = EnchantSlotCapacityTypes.REFERENCE

    override fun getCapacity(enchantment: Enchantment): Int {
        return 1 // TODO 实现引用功能
    }
}
