package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.item.KoishItemProxy
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE
import cc.mewcraft.wakame.util.adventure.asMinimalStringKoish
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.InvalidKeyException
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 用于识别一个自定义物品的类型.
 *
 * @param id 物品类型的 ID, 可用于 Registry 查询
 */
@ConsistentCopyVisibility
data class ItemKey
private constructor(
    val id: KoishKey,
) {

    companion object {

        @JvmField
        val CODEC: Codec<ItemKey> = Codec.STRING.comapFlatMap(
            { string ->
                try {
                    DataResult.success(of(KoishKeys.of(string)))
                } catch (_: InvalidKeyException) {
                    DataResult.error { "Invalid identifier string: $string" }
                }
            },
            ItemKey::asString
        )

        @JvmStatic
        private val objectPool: HashMap<KoishKey, ItemKey> = HashMap()

        @JvmStatic
        fun of(id: KoishKey): ItemKey {
            return objectPool.computeIfAbsent(id, ::ItemKey)
        }

        @JvmStatic
        fun serializer(): SimpleSerializer<ItemKey> {
            return Serializer
        }
    }

    // 将 RegistryEntry 设置为成员, 以省去运行时从 Registry 查询的性能开销
    val itemType: KoishItem by when (id.namespace()) {
        KOISH_NAMESPACE -> runCatching { BuiltInRegistries.ITEM.createEntry(id) }.getOrElse {
            objectPool.remove(id) // 可以释放内存?
            LOGGER.error("Failed to find ${KoishItem::class.simpleName} entry with id: $id, using empty item instead")
            RegistryEntry.Direct(KoishItem.EMPTY)
        }

        MINECRAFT_NAMESPACE -> runCatching { BuiltInRegistries.ITEM_PROXY.createEntry(id) }.getOrElse {
            objectPool.remove(id)
            LOGGER.error("Failed to find ${KoishItemProxy::class.simpleName} entry with id: $id, using empty item proxy instead")
            RegistryEntry.Direct(KoishItemProxy.EMPTY)
        }

        else -> error("Unrecognized namespace of item type: $id")
    }

    fun asString(): String {
        return id.asMinimalStringKoish()
    }

    // 该序列化操作使用对象池来返回 ItemId 的实例
    private object Serializer : SimpleSerializer<ItemKey> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemKey {
            return of(node.require<KoishKey>())
        }

        override fun serialize(type: Type, obj: ItemKey?, node: ConfigurationNode) {
            if (obj == null) return
            else node.set(obj.id.asMinimalStringKoish())
        }
    }
}