package cc.mewcraft.wakame.attribute

import org.spongepowered.configurate.ConfigurationNode

/**
 * 属性在配置文件中的序列化操作。
 *
 * @param T 属性的类型，详见 [SchemeAttributeValue] 的实现类
 */
interface AttributeConfigSerializer<T : SchemeAttributeValue> {
    /**
     * 将 [value] 写进 [node]
     */
    fun serialize(node: ConfigurationNode, value: T) {
        throw UnsupportedOperationException()
    }

    /**
     * 读取 [node] 并返回 [T]
     */
    fun deserialize(node: ConfigurationNode): T
}