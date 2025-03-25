package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.register
import com.github.quillraven.fleks.Component
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

sealed interface AbilityMetaContainer : Iterable<AbilityMetaType<*>> {

    companion object {
        @JvmField
        val EMPTY: AbilityMetaContainer = EmptyAbilityMetaContainer

        fun makeSerializers(): TypeSerializerCollection {
            val collection = TypeSerializerCollection.builder()
            collection.register<AbilityMetaContainer>(SimpleAbilityMetaContainer.Serializer)
            collection.registerAll(AbilityMetaTypes.directSerializers())
            return collection.build()
        }

        fun build(block: Builder.() -> Unit): AbilityMetaContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return SimpleAbilityMetaContainer()
        }
    }

    /**
     * 获取 [AbilityMetaType] 对应的 [Component].
     *
     * @param V 数据类型
     */
    operator fun <V : Component<V>> get(type: AbilityMetaType<V>): V?

    /**
     * [AbilityMetaContainer] 的生成器.
     */
    sealed interface Builder : AbilityMetaContainer {

        /**
         * 设置 [AbilityMetaType] 对应的 [Component].
         */
        operator fun <V : Component<V>> set(type: AbilityMetaType<V>, value: V)

        /**
         * 设置 [AbilityMetaType] 对应的 [Component].
         */
        @ApiStatus.Internal
        fun setUnsafe(type: AbilityMetaType<*>, value: Any)

        /**
         * 以当前状态创建一个 [AbilityMetaContainer] 实例.
         *
         * @return 当前实例
         */
        fun build(): AbilityMetaContainer

    }
}

// ------------
// 内部实现
// ------------

private data object EmptyAbilityMetaContainer : AbilityMetaContainer {
    override fun <V : Component<V>> get(type: AbilityMetaType<V>): V? = null
    override fun iterator(): Iterator<AbilityMetaType<*>> = emptyList<AbilityMetaType<*>>().iterator()
}

private class SimpleAbilityMetaContainer(
    private val metaMap: Reference2ObjectLinkedOpenHashMap<AbilityMetaType<*>, Component<*>> = Reference2ObjectLinkedOpenHashMap(),
) : AbilityMetaContainer, AbilityMetaContainer.Builder {

    override fun <V : Component<V>> get(type: AbilityMetaType<V>): V? {
        return metaMap[type] as V?
    }

    override fun <V : Component<V>> set(type: AbilityMetaType<V>, value: V) {
        metaMap[type] = value
    }

    override fun setUnsafe(type: AbilityMetaType<*>, value: Any) {
        metaMap[type] = value as Component<*>
    }

    override fun build(): AbilityMetaContainer {
        return if (metaMap.isEmpty()) AbilityMetaContainer.EMPTY else this
    }

    override fun iterator(): ObjectIterator<AbilityMetaType<*>> {
        return metaMap.keys.iterator()
    }

    object Serializer : TypeSerializer2<AbilityMetaContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): AbilityMetaContainer {
            val builder = AbilityMetaContainer.builder()
            for ((rawNodeKey, node) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                val dataType = KoishRegistries2.ABILITY_META_TYPE[nodeKey]
                if (dataType == null) {
                    LOGGER.warn("Unknown ability meta type '$nodeKey'. Skipped.")
                    continue
                }
                val dataValue = node.get(dataType.typeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType. Skipped.")
                    continue
                }
                builder.setUnsafe(dataType, dataValue)
            }
            return builder.build()
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): AbilityMetaContainer? {
            return AbilityMetaContainer.EMPTY
        }
    }

}