package cc.mewcraft.wakame.serialization.configurate.extension

import cc.mewcraft.wakame.util.collection.filterKeysNotNull
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 将 [Map] 的 [Map.keys] 转换为类型 [U].
 *
 * [Map.keys] 在 [ConfigurationNode.options] 中必须存在对应的 [ScalarSerializer],
 * 否则会在转换过程中被省略或直接抛出异常 (依据传入的 [throwIfFail]).
 *
 * @param throwIfFail 是否在转换失败时抛出异常. 如果为 false, 则返回转换失败的 [Map.Entry] 将被过滤掉
 */
inline fun <reified U> Map<Any, ConfigurationNode>.transformKeys(throwIfFail: Boolean = true): Map<U, ConfigurationNode> {
    return transformKeys(typeOf<U>(), throwIfFail)
}

@PublishedApi
internal fun <U> Map<Any, ConfigurationNode>.transformKeys(type: KType, throwIfFail: Boolean): Map<U, ConfigurationNode> {
    val result = mapKeys { (nodeKey, node) ->
        val serializer = node.options().serializers().get(type) as? ScalarSerializer<U>
        if (serializer == null) {
            if (throwIfFail) throw IllegalStateException("No such scalar serializer to transform node key '$nodeKey' to type '$type'") else null
        } else {
            try {
                serializer.deserialize(nodeKey) as U
            } catch (e: Exception) {
                if (throwIfFail) throw IllegalStateException("Failed to transform node key '$nodeKey' to type '$type") else null
            }
        }
    }
    if (throwIfFail) {
        // 优化: 如果 throwIfFail=true 则原 Map 不可能包含 null key, 可以直接 cast
        return result as Map<U, ConfigurationNode>
    } else {
        return result.filterKeysNotNull()
    }
}