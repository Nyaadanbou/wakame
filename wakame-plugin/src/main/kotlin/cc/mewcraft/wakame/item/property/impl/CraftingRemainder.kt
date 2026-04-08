package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.api.Koish
import cc.mewcraft.wakame.bridge.MojangStack
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.item.toNMS
import net.minecraft.resources.Identifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import net.minecraft.core.registries.BuiltInRegistries as MojangBuiltInRegistries

sealed interface CraftingRemainder {
    /**
     * 获取单个输入物品参与合成后返还的物品.
     */
    fun remainder(inputStack: MojangStack): MojangStack

    // 用于序列化
    val type: CraftingRemainderType

    companion object {
        fun serializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<CraftingRemainderType>(BuiltInRegistries.CRAFTING_REMAINDER_TYPE.valueByNameTypeSerializer())
            serials.registerExact<CraftingRemainder>(DispatchingSerializer.create(CraftingRemainder::type, CraftingRemainderType::kotlinType))
            return serials.build()
        }
    }
}

class CraftingRemainderType(internal val kotlinType: KType)

object CraftingRemainderTypes {

    @JvmField
    val ITEM: CraftingRemainderType = register<ItemRemainder>("item")

    @JvmField
    val HURT_AND_BREAK: CraftingRemainderType = register<HurtAndBreakRemainder>("hurt_and_break")

    private inline fun <reified E : CraftingRemainder> register(id: String): CraftingRemainderType {
        return Registry.register(BuiltInRegistries.CRAFTING_REMAINDER_TYPE, id, CraftingRemainderType(typeOf<E>()))
    }
}

/**
 * 返还特定的物品.
 */
@ConfigSerializable
data class ItemRemainder(
    val id: KoishKey,
    val amount: Int = 1,
) : CraftingRemainder {
    override val type: CraftingRemainderType = CraftingRemainderTypes.ITEM

    override fun remainder(inputStack: MojangStack): MojangStack {
        val koishItem = Koish.get().getItemRegistry().getOrNull(id);
        if (koishItem != null) {
            return koishItem.createItemStack(amount).toNMS()
        } else {
            if (id.namespace() == KoishKey.MINECRAFT_NAMESPACE) {
                val resourceLocation = Identifier.withDefaultNamespace(id.value())
                // identifier 对应的原版物品未找到的话, 会返回空气且无警告
                val item = MojangBuiltInRegistries.ITEM.getValue(resourceLocation)
                return MojangStack(item, amount)
            }
            LOGGER.warn("No item type with id: $id, crafting remainder will be empty")
            return MojangStack.EMPTY
        }
    }
}

/**
 * 返还原物品在扣除耐久后产生的新物品.
 *
 * 注意事项:
 * - 如耐久归零, 则返还空气.
 * - 如扣除耐久失败, 则返还原物品的克隆.
 */
@ConfigSerializable
data class HurtAndBreakRemainder(
    val damage: Int,
) : CraftingRemainder {
    override val type: CraftingRemainderType = CraftingRemainderTypes.HURT_AND_BREAK

    override fun remainder(inputStack: MojangStack): MojangStack {
        val remainderStack = inputStack.copy()
        // 想了一下还是不走服务端玩家使用物品时掉耐久的逻辑
        // 直接数值上扣除耐久比较不容易出错
        if (inputStack.isDamageableItem) {
            val currentDamage = remainderStack.damageValue
            val newDamage = currentDamage + damage
            if (newDamage >= remainderStack.maxDamage) {
                return MojangStack.EMPTY
            } else {
                remainderStack.damageValue = newDamage
                return remainderStack
            }
        } else {
            return remainderStack
        }
    }
}