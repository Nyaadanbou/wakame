package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import com.github.quillraven.fleks.Component
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

sealed interface AbilityMetaType<V : Component<V>> {

    companion object {

        fun makeSerializer(): ScalarSerializer<AbilityMetaType<*>> {
            return KoishRegistries2.ABILITY_META_TYPE.valueByNameTypeSerializer()
        }

        fun <V : Component<V>> builder(typeToken: TypeToken<V>): Builder<V> {
            return Builder(typeToken)
        }

    }

    val typeToken: TypeToken<V>

    /**
     * 返回空表示数据类型 [U] 可以直接使用现有的 [TypeSerializerCollection] 来完成序列化操作.
     * 但如果数据类型 [U] 需要依赖额外的 [TypeSerializer] 来完成序列化操作, 可以在这里返回.
     */
    val serializers: TypeSerializerCollection?

    class Builder<V : Component<V>>(
        private val typeToken: TypeToken<V>,
    ) {
        private var serializers: TypeSerializerCollection? = null

        fun serializers(serializers: TypeSerializerCollection): Builder<V> {
            this.serializers = serializers
            return this
        }

        fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): Builder<V> {
            this.serializers = TypeSerializerCollection.builder().apply(block).build()
            return this
        }

        fun build(): AbilityMetaType<V> {
            return Simple(typeToken, serializers)
        }

        private class Simple<V : Component<V>>(
            override val typeToken: TypeToken<V>,
            override val serializers: TypeSerializerCollection?,
        ) : AbilityMetaType<V> {
            override fun toString(): String = "AbilityMetaType(type=${typeToken.type}, serializers=$serializers)"
        }
    }
}