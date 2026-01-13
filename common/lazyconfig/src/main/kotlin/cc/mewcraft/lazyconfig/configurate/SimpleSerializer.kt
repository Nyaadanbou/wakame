package cc.mewcraft.lazyconfig.configurate

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type

fun interface SimpleSerializer<T> : TypeSerializer<T> {

    override fun deserialize(type: Type, node: ConfigurationNode): T?
    override fun deserialize(type: AnnotatedType, node: ConfigurationNode): T? = super.deserialize(type, node)
    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Unit = throw NotImplementedError() // 大多数实现并不会用到这个, 默认无需实现但抛出异常
    override fun serialize(type: AnnotatedType, obj: T?, node: ConfigurationNode) = super.serialize(type, obj, node)
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): T? = super.emptyValue(specificType, options)
    override fun emptyValue(specificType: AnnotatedType, options: ConfigurationOptions): T? = super.emptyValue(specificType, options)

}