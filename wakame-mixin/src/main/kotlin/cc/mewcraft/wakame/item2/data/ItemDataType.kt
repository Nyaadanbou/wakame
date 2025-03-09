package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import com.mojang.serialization.Codec
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.TypeSerializerCollection

interface ItemDataType<T> {

    companion object {

        @JvmField
        val CODEC: Codec<ItemDataType<*>> = TODO()

        // FIXME ItemDataType 的 TypeSerializer 应该是从:
        //  Identifier <-> ItemDataType 之间进行转换,
        //  所以应该从 registry 生成一个 TypeSerializer
        @JvmField
        val SERIALIZER: TypeSerializer<ItemDataType<*>> = TODO()

        fun <T> builder(typeToken: TypeToken<T>): Builder<T> {
            return Builder(typeToken)
        }

    }

    // FIXME: 类型 T
    val typeToken: TypeToken<T>

    // FIXME: 返回空则表示该类型的实例可以直接使用 ObjectMapper.
    //  但如果 ObjectMapper<T> 必须依赖其他的 TypeSerializer 工作,
    //  则可以在这里传入那些依赖的 TypeSerializer
    val serializers: TypeSerializerCollection?

    class Builder<T>(
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

        fun build(): ItemDataType<T> {
            return Simple(typeToken, serializers)
        }

        private class Simple<T>(
            override val typeToken: TypeToken<T>,
            override val serializers: TypeSerializerCollection?,
        ) : ItemDataType<T> {
            override fun toString(): String = "Simple(type=${typeToken.type}, serializers=$serializers)"
        }
    }

}