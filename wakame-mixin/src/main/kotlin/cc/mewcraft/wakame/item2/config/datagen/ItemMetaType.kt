package cc.mewcraft.wakame.item2.config.datagen

import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 代表一个 "Item Data" 的元数据, 即 *如何生成一个数据*.
 *
 * @param U 配置类型, 即 [ItemMetaEntry] 的实现类
 * @param V 数据类型, 即 [配置类型][U] 对应的 *数据类型*
 */
interface ItemMetaType<U, V> {

    companion object {
        fun <U, V> builder(typeToken: TypeToken<U>): Builder<U, V> {
            return Builder(typeToken)
        }
    }

    val typeToken: TypeToken<U>

    val serializers: TypeSerializerCollection?

    /**
     * @param U 配置类型, 即 [ItemMetaEntry] 的实现类
     * @param V 数据类型, 即 [配置类型][U] 对应的 *数据类型*
     */
    class Builder<U, V>(
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

        fun build(): ItemMetaType<U, V> {
            return Simple(typeToken, serializers)
        }

        private class Simple<U, V>(
            override val typeToken: TypeToken<U>,
            override val serializers: TypeSerializerCollection?,
        ) : ItemMetaType<U, V> {
            override fun toString(): String {
                return "ItemMetaType(type=${typeToken.type}, serializers=$serializers)"
            }
        }
    }

}