package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * @param T 数据类型
 */
interface ItemPropertyType<T> {

    companion object {

        // FIXME #350:
        //  Identifier <-> GlobalPropertyType 之间转换,
        //  所以应该从 Registry 生成一个 TypeSerializer
        fun makeSerializer(): ScalarSerializer<ItemPropertyType<*>> {
            return KoishRegistries2.ITEM_PROPERTY_TYPE.valueByNameTypeSerializer()
        }

        fun <T> builder(typeToken: TypeToken<T>): Builder<T> {
            return Builder(typeToken)
        }

    }

    val typeToken: TypeToken<T>

    // FIXME #350: 返回空则表示 ItemDataType<T> 中的 T 可以直接使用 ObjectMapper<T>.
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

        fun build(): ItemPropertyType<T> {
            return Simple(typeToken, serializers)
        }

        private class Simple<T>(
            override val typeToken: TypeToken<T>,
            override val serializers: TypeSerializerCollection?,
        ) : ItemPropertyType<T> {
            override fun toString(): String = "ItemPropertyType(type=${typeToken.type}, serializers=$serializers)"
        }
    }

}