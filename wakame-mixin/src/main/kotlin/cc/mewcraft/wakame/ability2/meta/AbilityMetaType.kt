package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import com.github.quillraven.fleks.Component
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 代表了一个技能类型, 可直接序列化成组件. 需要要求组件 [T] 是可序列化的.
 */
sealed interface AbilityMetaType<T : Component<T>> {

    companion object {

        fun makeSerializer(): ScalarSerializer<AbilityMetaType<*>> {
            return KoishRegistries2.ABILITY_META_TYPE.valueByNameTypeSerializer()
        }

        fun <T : Component<T>> builder(typeToken: TypeToken<T>): Builder<T> {
            return Builder(typeToken)
        }

    }

    val typeToken: TypeToken<T>

    /**
     * 返回空表示数据类型 [T] 可以直接使用现有的 [TypeSerializerCollection] 来完成序列化操作.
     * 但如果数据类型 [T] 需要依赖额外的 [TypeSerializer] 来完成序列化操作, 可以在这里返回.
     */
    val serializers: TypeSerializerCollection?

    class Builder<T : Component<T>>(
        private val typeToken: TypeToken<T>,
    ) {
        private var serializers: TypeSerializerCollection? = null

        fun serializers(serializers: TypeSerializerCollection): Builder<T> {
            this.serializers = serializers
            return this
        }

        fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): Builder<T> {
            this.serializers = TypeSerializerCollection.builder().apply(block).build()
            return this
        }

        fun build(): AbilityMetaType<T> {
            return Simple(typeToken, serializers)
        }

        private class Simple<T : Component<T>>(
            override val typeToken: TypeToken<T>,
            override val serializers: TypeSerializerCollection?,
        ) : AbilityMetaType<T> {
            override fun toString(): String = "AbilityMetaType(type=${typeToken.type}, serializers=$serializers)"
        }
    }
}