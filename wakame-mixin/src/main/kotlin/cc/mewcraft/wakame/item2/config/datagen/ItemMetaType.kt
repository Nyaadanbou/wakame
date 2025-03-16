package cc.mewcraft.wakame.item2.config.datagen

import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.TypeSerializerCollection

interface ItemMetaType<T> {

    companion object {
        fun <T> builder(typeToken: TypeToken<T>): Builder<T> {
            return Builder(typeToken)
        }
    }

    val typeToken: TypeToken<T>

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

        fun build(): ItemMetaType<T> {
            return Simple(typeToken, serializers)
        }

        private class Simple<T>(
            override val typeToken: TypeToken<T>,
            override val serializers: TypeSerializerCollection?,
        ) : ItemMetaType<T> {
            override fun toString(): String {
                return "ItemMetaType(type=${typeToken.type}, serializers=$serializers)"
            }
        }
    }

}