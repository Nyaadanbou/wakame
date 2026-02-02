package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.serializer.IdentifierSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE
import cc.mewcraft.wakame.util.adventure.asMinimalStringKoish
import cc.mewcraft.wakame.util.typeTokenOf
import com.mojang.serialization.Codec
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.extra.dfu.v8.DfuSerializers
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

/**
 * 用于识别一个自定义物品的类型.
 *
 * @param id 物品类型的 ID, 可用于 Registry 查询
 */
@ConsistentCopyVisibility
data class ItemKey
private constructor(
    val id: Identifier,
) {

    companion object {

        @JvmStatic
        private val objectPool: HashMap<Identifier, ItemKey> = HashMap()

        @JvmStatic
        fun of(id: Identifier): ItemKey {
            return objectPool.computeIfAbsent(id, ::ItemKey)
        }

        @JvmStatic
        fun serializer(): SimpleSerializer<ItemKey> {
            return Serializer
        }

        @JvmStatic
        fun makeCodec(): Codec<ItemKey> {
            val codec = DfuSerializers.codec(
                typeTokenOf<ItemKey>(), TypeSerializerCollection.builder()
                    .register(Serializer)
                    .register(IdentifierSerializer)
                    .build()
            )
            requireNotNull(codec) { "Cannot find an appropriate TypeSerializer for ${ItemKey::class}" }
            return codec
        }
    }

    // 将 RegistryEntry 设置为成员, 以省去运行时从 Registry 查询的性能开销
    val itemType: KoishItem by when (id.namespace()) {
        KOISH_NAMESPACE -> BuiltInRegistries.ITEM.createEntry(id)
        MINECRAFT_NAMESPACE -> BuiltInRegistries.ITEM_PROXY.createEntry(id)
        else -> error("Unrecognized namespace of item type: $id")
    }

    // 该序列化操作使用对象池来返回 ItemId 的实例
    private object Serializer : SimpleSerializer<ItemKey> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemKey {
            return of(node.require<Identifier>())
        }

        override fun serialize(type: Type, obj: ItemKey?, node: ConfigurationNode) {
            if (obj == null) return
            else node.set(obj.id.asMinimalStringKoish())
        }
    }
}