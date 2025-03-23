package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

sealed interface AbilityMetaContainer {

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
     * 获取 [AbilityMetaType] 对应的 [AbilityMetaEntry].
     *
     * @param U 配置类型, 即 [AbilityMetaEntry] 的实现类
     * @param V 数据类型, 即 [配置类型][U] 对应的 *数据类型*
     */
    operator fun <U : AbilityMetaEntry<V>, V> get(type: AbilityMetaType<U, V>): AbilityMetaEntry<V>?

    /**
     * [AbilityMetaContainer] 的生成器.
     */
    sealed interface Builder : AbilityMetaContainer {

        /**
         * 设置 [AbilityMetaType] 对应的 [AbilityMetaEntry].
         */
        operator fun <U : AbilityMetaEntry<V>, V> set(type: AbilityMetaType<U, V>, value: AbilityMetaEntry<V>)

        /**
         * 设置 [AbilityMetaType] 对应的 [AbilityMetaEntry].
         */
        @ApiStatus.Internal
        fun setUnsafe(type: AbilityMetaType<*, *>, value: Any)

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
    override fun <U : AbilityMetaEntry<V>, V> get(type: AbilityMetaType<U, V>): AbilityMetaEntry<V>? = null
}

private class SimpleAbilityMetaContainer(
    private val metaMap: Reference2ObjectLinkedOpenHashMap<AbilityMetaType<*, *>, AbilityMetaEntry<*>> = Reference2ObjectLinkedOpenHashMap(),
) : AbilityMetaContainer, AbilityMetaContainer.Builder {

    override fun <U : AbilityMetaEntry<V>, V> get(type: AbilityMetaType<U, V>): AbilityMetaEntry<V>? {
        return metaMap[type] as AbilityMetaEntry<V>?
    }

    override fun <U : AbilityMetaEntry<V>, V> set(type: AbilityMetaType<U, V>, value: AbilityMetaEntry<V>) {
        metaMap.put(type, value)
    }

    override fun setUnsafe(type: AbilityMetaType<*, *>, value: Any) {
        // 警告: 这里无法对 value: Any 中的泛型参数做检查, 实现需要保证 value 的类型完全正确
        metaMap.put(type, value as AbilityMetaEntry<*>)
    }

    override fun build(): AbilityMetaContainer {
        return if (metaMap.isEmpty()) AbilityMetaContainer.EMPTY else this
    }

    object Serializer : TypeSerializer2<AbilityMetaContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): AbilityMetaContainer {
            val builder = AbilityMetaContainer.builder()
            for ((rawNodeKey, node) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                val dataType = KoishRegistries2.ABILITY_META_TYPE[nodeKey] ?: continue
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