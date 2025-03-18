package cc.mewcraft.wakame.item2.data.impl

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 用于识别一个自定义物品的类型.
 *
 * @param id 物品类型的 ID, 可用于 Registry 查询
 */
@ConsistentCopyVisibility
data class ItemId
private constructor(
    val id: Identifier,
) {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer<ItemId> = Serializer

        private val POOL: HashMap<Identifier, ItemId> = HashMap<Identifier, ItemId>()

        fun of(id: Identifier): ItemId {
            return POOL.computeIfAbsent(id, ::ItemId)
        }

    }

    // 将 RegistryEntry 设置为成员, 以省去运行时从 Registry 查询的性能开销
    val itemType: KoishItem by when (id.namespace()) {
        KOISH_NAMESPACE -> KoishRegistries2.ITEM.createEntry(id)
        MINECRAFT_NAMESPACE -> KoishRegistries2.ITEM_PROXY.createEntry(id)
        else -> error("Unrecognized namespace of item type: $id")
    }

    // 该序列化操作使用对象池来返回 ItemId 的实例
    private object Serializer : TypeSerializer<ItemId> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemId {
            return of(node.require<Identifier>())
        }

        override fun serialize(type: Type, obj: ItemId?, node: ConfigurationNode) {
            if (obj == null) return
            else node.set(obj.id)
        }
    }

}
