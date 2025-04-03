package cc.mewcraft.wakame.item2.config.datagen

import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 代表一个*持久化数据类型*的元数据的类型. 在这里“元数据”意为“如何生成数据”.
 *
 * @param U 元数据的类型, 即 [ItemMetaEntry] 的实现类
 * @param V 持久化数据的类型, 即 [元数据类型][U] 对应的*数据类型*
 */
sealed interface ItemMetaType<U, V> {

    companion object {
        inline fun <reified U, V> builder(): Builder<U, V> {
            return Builder(typeOf<U>())
        }
    }

    val kotlinType: KType

    /**
     * 返回空表示数据类型 [U] 可以直接使用现有的 [TypeSerializerCollection] 来完成序列化操作.
     * 但如果数据类型 [U] 需要依赖额外的 [TypeSerializer] 来完成序列化操作, 可以在这里返回.
     */
    val serializers: TypeSerializerCollection?

    /**
     * @param U 元数据的类型, 即 [ItemMetaEntry] 的实现类
     * @param V 持久化数据的类型, 即 [元数据类型][U] 对应的*数据类型*
     */
    class Builder<U, V>(
        private val kotlinType: KType, // KType<U>
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
            return Simple(kotlinType, serializers)
        }

        private class Simple<U, V>(
            override val kotlinType: KType, // KType<U>
            override val serializers: TypeSerializerCollection?,
        ) : ItemMetaType<U, V> {
            override fun toString(): String {
                return "ItemMetaType(type=${kotlinType}, serializers=$serializers)"
            }
        }
    }

}