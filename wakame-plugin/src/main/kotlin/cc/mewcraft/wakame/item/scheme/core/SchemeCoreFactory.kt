package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.ability.SchemeAbilityValue
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.facade.SchemeAttributeValue
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.getOrThrow
import cc.mewcraft.wakame.util.requireKt
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
    fun schemeOf(node: ConfigurationNode): SchemeCore {
        val key = node.node("key").requireKt<Key>()
        val ret: SchemeCore = when (key.namespace()) {
            NekoNamespaces.ABILITY -> {
                val builder = @OptIn(InternalApi::class) AbilityRegistry.schemeBuilderRegistry.getOrThrow(key)
                val value = builder.build(node) as SchemeAbilityValue
                SchemeAbilityCore(key, value)
            }

            NekoNamespaces.ATTRIBUTE -> {
                val builder = @OptIn(InternalApi::class) AttributeRegistry.schemeBuilderRegistry.getOrThrow(key)
                val value = builder.build(node) as SchemeAttributeValue
                SchemeAttributeCore(key, value)
            }

            else -> throw IllegalArgumentException()
        }

        return ret
    }
}