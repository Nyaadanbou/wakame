package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.ability.AbilityFacadeRegistry
import cc.mewcraft.wakame.ability.SchemeAbilityValue
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.facade.AttributeFacadeRegistry
import cc.mewcraft.wakame.attribute.facade.SchemeAttributeValue
import cc.mewcraft.wakame.util.getOrThrow
import cc.mewcraft.wakame.util.typedRequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A factory used to create [SchemeCore].
 */
object SchemeCoreFactory {

    /**
     * 由配置文件调用，因此已知：
     * - [ConfigurationNode]
     *
     * 由此已知：
     * - Namespace
     * - Value
     *
     * 通过 Namespace + Value 我们可以唯一确定用什么实现来反序列化该 [ConfigurationNode].
     */
    @OptIn(InternalApi::class)
    fun schemeOf(node: ConfigurationNode): SchemeCore {
        val key = node.node("key").typedRequire<Key>()
        val ret: SchemeCore = when (key.namespace()) {
            NekoNamespaces.ABILITY -> {
                val builder = AbilityFacadeRegistry.schemeBuilderRegistry.getOrThrow(key)
                val value = builder.build(node)
                SchemeAbilityCore(key, value as SchemeAbilityValue)
            }

            NekoNamespaces.ATTRIBUTE -> {
                val builder = AttributeFacadeRegistry.schemeBuilderRegistry.getOrThrow(key)
                val value = builder.build(node)
                SchemeAttributeCore(key, value as SchemeAttributeValue)
            }

            else -> throw IllegalArgumentException()
        }

        return ret
    }
}