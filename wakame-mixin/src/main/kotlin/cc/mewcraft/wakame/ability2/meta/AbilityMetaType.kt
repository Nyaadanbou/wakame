package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

sealed interface AbilityMetaType<U : AbilityMetaEntry<V>, V> {

    companion object {

        fun makeSerializer(): ScalarSerializer<AbilityMetaType<*, *>> {
            return KoishRegistries2.ABILITY_META_TYPE.valueByNameTypeSerializer()
        }

        fun <U : AbilityMetaEntry<V>, V> builder(typeToken: TypeToken<U>): Builder<U, V> {
            return Builder(typeToken)
        }

    }

    val typeToken: TypeToken<U>

    /**
     * 返回空表示数据类型 [U] 可以直接使用现有的 [TypeSerializerCollection] 来完成序列化操作.
     * 但如果数据类型 [U] 需要依赖额外的 [TypeSerializer] 来完成序列化操作, 可以在这里返回.
     */
    val serializers: TypeSerializerCollection?

    class Builder<U : AbilityMetaEntry<V>, V>(
        private val typeToken: TypeToken<U>,
    ) {
        private var serializers: TypeSerializerCollection? = null

        fun serializers(serializers: TypeSerializerCollection): Builder<U, V> {
            this.serializers = serializers
            return this
        }

        fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): Builder<U, V> {
            this.serializers = TypeSerializerCollection.builder().apply(block).build()
            return this
        }

        fun build(): AbilityMetaType<U, V> {
            return Simple(typeToken, serializers)
        }

        private class Simple<U : AbilityMetaEntry<V>, V>(
            override val typeToken: TypeToken<U>,
            override val serializers: TypeSerializerCollection?,
        ) : AbilityMetaType<U, V> {
            override fun toString(): String = "AbilityMetaType(type=${typeToken.type}, serializers=$serializers)"
        }
    }
}